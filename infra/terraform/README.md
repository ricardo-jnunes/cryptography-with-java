# IaC da demo mTLS (Terraform)

Automatiza a Parte B de [`docs/AWS-MTLS-SECRETS-MANAGER.md`](../../docs/AWS-MTLS-SECRETS-MANAGER.md).
Use uma conta de **dev/sandbox**.

## Recursos criados
- S3 privado e versionado com o `truststore.pem`
- Certificado ACM (validação DNS no Route 53)
- API Gateway REST regional: `GET /hello` com Mock 200, endpoint `execute-api` desabilitado
- Custom domain TLS 1.2 com mTLS, mapping e registro A (alias)
- Secret vazio `hands-on/rsa-private-key` e política IAM de leitura (`GetSecretValue`)

## Pré-requisitos
- Terraform >= 1.5 e credenciais AWS configuradas
- Hosted zone pública existente no Route 53
- Material criptográfico da Parte A do guia (fora do repositório)

## Uso
```powershell
cd infra\terraform
copy terraform.tfvars.example terraform.tfvars   # edite os valores
terraform init
terraform apply
```

Grave a chave privada no secret (o valor não passa pelo Terraform nem pelo state):
```powershell
aws secretsmanager put-secret-value --region us-east-1 --secret-id hands-on/rsa-private-key --secret-string file://app-privada.pem
del app-privada.pem
```

## Validação
```powershell
curl --cert client.pem --key client.key https://mtls-demo.exemplo.com/hello   # 200 {"status":"ok",...}
curl https://mtls-demo.exemplo.com/hello                                      # falha no handshake
```
A app local (`:8050`) lê o secret com as credenciais do seu perfil AWS.

## Limpeza
```powershell
terraform destroy
```
O state guarda ARNs e nomes, mas nenhuma chave privada. Não versione `*.tfstate` nem `terraform.tfvars`.
