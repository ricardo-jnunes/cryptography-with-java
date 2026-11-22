data "aws_caller_identity" "current" {}

data "aws_route53_zone" "this" {
  name         = var.hosted_zone_name
  private_zone = false
}

# ---------------------------------------------------------------------------
# S3: truststore (somente certificados públicos da CA)
# ---------------------------------------------------------------------------
resource "aws_s3_bucket" "truststore" {
  bucket_prefix = "mtls-demo-truststore-"
  force_destroy = true # demo descartável: permite destroy com versões dentro
}

resource "aws_s3_bucket_public_access_block" "truststore" {
  bucket                  = aws_s3_bucket.truststore.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_versioning" "truststore" {
  bucket = aws_s3_bucket.truststore.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_object" "truststore" {
  bucket = aws_s3_bucket.truststore.id
  key    = "truststore.pem"
  source = var.truststore_pem_path
  etag   = filemd5(var.truststore_pem_path)

  depends_on = [aws_s3_bucket_versioning.truststore]
}

# ---------------------------------------------------------------------------
# ACM: certificado do domínio (validação por DNS)
# ---------------------------------------------------------------------------
resource "aws_acm_certificate" "this" {
  domain_name       = var.domain_name
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_route53_record" "cert_validation" {
  for_each = {
    for dvo in aws_acm_certificate.this.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      type   = dvo.resource_record_type
      record = dvo.resource_record_value
    }
  }

  zone_id         = data.aws_route53_zone.this.zone_id
  name            = each.value.name
  type            = each.value.type
  records         = [each.value.record]
  ttl             = 60
  allow_overwrite = true
}

resource "aws_acm_certificate_validation" "this" {
  certificate_arn         = aws_acm_certificate.this.arn
  validation_record_fqdns = [for r in aws_route53_record.cert_validation : r.fqdn]
}

# ---------------------------------------------------------------------------
# API Gateway REST (Regional) com GET /hello -> Mock 200
# ---------------------------------------------------------------------------
resource "aws_api_gateway_rest_api" "this" {
  name = "mtls-demo"

  # Sem isso, https://<id>.execute-api... fica aberto sem mTLS.
  disable_execute_api_endpoint = true

  endpoint_configuration {
    types = ["REGIONAL"]
  }
}

resource "aws_api_gateway_resource" "hello" {
  rest_api_id = aws_api_gateway_rest_api.this.id
  parent_id   = aws_api_gateway_rest_api.this.root_resource_id
  path_part   = "hello"
}

resource "aws_api_gateway_method" "hello_get" {
  rest_api_id   = aws_api_gateway_rest_api.this.id
  resource_id   = aws_api_gateway_resource.hello.id
  http_method   = "GET"
  authorization = "NONE" # autenticação é feita pelo mTLS no custom domain
}

resource "aws_api_gateway_integration" "hello_get" {
  rest_api_id = aws_api_gateway_rest_api.this.id
  resource_id = aws_api_gateway_resource.hello.id
  http_method = aws_api_gateway_method.hello_get.http_method
  type        = "MOCK"

  request_templates = {
    "application/json" = jsonencode({ statusCode = 200 })
  }
}

resource "aws_api_gateway_method_response" "hello_get_200" {
  rest_api_id = aws_api_gateway_rest_api.this.id
  resource_id = aws_api_gateway_resource.hello.id
  http_method = aws_api_gateway_method.hello_get.http_method
  status_code = "200"

  response_models = {
    "application/json" = "Empty"
  }
}

resource "aws_api_gateway_integration_response" "hello_get_200" {
  rest_api_id = aws_api_gateway_rest_api.this.id
  resource_id = aws_api_gateway_resource.hello.id
  http_method = aws_api_gateway_method.hello_get.http_method
  status_code = aws_api_gateway_method_response.hello_get_200.status_code

  response_templates = {
    "application/json" = jsonencode({ status = "ok", mensagem = "mTLS validado" })
  }

  depends_on = [aws_api_gateway_integration.hello_get]
}

resource "aws_api_gateway_deployment" "this" {
  rest_api_id = aws_api_gateway_rest_api.this.id

  triggers = {
    redeployment = sha1(jsonencode([
      aws_api_gateway_resource.hello.id,
      aws_api_gateway_method.hello_get.id,
      aws_api_gateway_integration.hello_get.id,
      aws_api_gateway_integration_response.hello_get_200.id,
      aws_api_gateway_integration_response.hello_get_200.response_templates,
      aws_api_gateway_rest_api.this.disable_execute_api_endpoint,
    ]))
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_api_gateway_stage" "this" {
  rest_api_id   = aws_api_gateway_rest_api.this.id
  deployment_id = aws_api_gateway_deployment.this.id
  stage_name    = var.stage_name
}

# ---------------------------------------------------------------------------
# Custom domain com mTLS + mapping + DNS
# ---------------------------------------------------------------------------
resource "aws_api_gateway_domain_name" "this" {
  domain_name              = var.domain_name
  regional_certificate_arn = aws_acm_certificate_validation.this.certificate_arn
  security_policy          = "TLS_1_2"

  endpoint_configuration {
    types = ["REGIONAL"]
  }

  mutual_tls_authentication {
    truststore_uri     = "s3://${aws_s3_bucket.truststore.id}/${aws_s3_object.truststore.key}"
    truststore_version = aws_s3_object.truststore.version_id
  }
}

resource "aws_api_gateway_base_path_mapping" "this" {
  api_id      = aws_api_gateway_rest_api.this.id
  stage_name  = aws_api_gateway_stage.this.stage_name
  domain_name = aws_api_gateway_domain_name.this.domain_name
}

resource "aws_route53_record" "api" {
  zone_id = data.aws_route53_zone.this.zone_id
  name    = var.domain_name
  type    = "A"

  alias {
    name                   = aws_api_gateway_domain_name.this.regional_domain_name
    zone_id                = aws_api_gateway_domain_name.this.regional_zone_id
    evaluate_target_health = false
  }
}

# ---------------------------------------------------------------------------
# Secrets Manager: só o "cofre". O valor é gravado fora do Terraform (CLI)
# para a chave privada não ir parar no state.
# ---------------------------------------------------------------------------
resource "aws_secretsmanager_secret" "rsa_private_key" {
  name                    = var.secret_name
  description             = "Chave privada RSA do payload (demo)"
  recovery_window_in_days = 0 # demo: apaga imediatamente no destroy
}

# ---------------------------------------------------------------------------
# IAM: permissão mínima para a app local
# ---------------------------------------------------------------------------
data "aws_iam_policy_document" "read_secret" {
  statement {
    effect    = "Allow"
    actions   = ["secretsmanager:GetSecretValue"]
    resources = [aws_secretsmanager_secret.rsa_private_key.arn]
  }
}

resource "aws_iam_policy" "read_secret" {
  name   = "mtls-demo-read-rsa-private-key"
  policy = data.aws_iam_policy_document.read_secret.json
}

resource "aws_iam_user_policy_attachment" "read_secret" {
  count      = var.attach_policy_to_user == "" ? 0 : 1
  user       = var.attach_policy_to_user
  policy_arn = aws_iam_policy.read_secret.arn
}
