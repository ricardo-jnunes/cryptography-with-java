# IaC da demo mTLS (Terraform)

Automatiza a Parte B de [`docs/AWS-MTLS-SECRETS-MANAGER.md`](../../docs/AWS-MTLS-SECRETS-MANAGER.md).
Use uma conta de **dev/sandbox**.

## Recursos criados
- S3 privado e versionado com o `truststore.pem`
- Certificado ACM (validação DNS no Route 53)
- API Gateway REST regional: `GET /hello` com Mock 200, endpoint `execute-api` desabilitado
- Custom domain TLS 1.2 com mTLS, mapping e registro A (alias)
- Secret vazio `hands-on/api-key` e política IAM de leitura (`GetSecretValue`), opcional via `create_read_policy`

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

Grave a API key (valor fictício) no secret. O valor não passa pelo Terraform nem pelo state:
```powershell
aws secretsmanager put-secret-value --region us-east-1 --secret-id hands-on/api-key --secret-string sk-demo-123456
```
Para a demo de rotação, rode de novo com outro valor: o `versionId` muda.

Sem permissão `iam:CreatePolicy`? Use `create_read_policy = false` no `terraform.tfvars`.

## Validação
```powershell
curl --cert client.pem --key client.key https://mtls-demo.exemplo.com/hello   # 200 {"status":"ok",...}
curl https://mtls-demo.exemplo.com/hello                                      # falha no handshake
```
No Windows, o `curl.exe` nativo usa Schannel e não lê `client.pem`/`client.key`. Teste com o `openssl s_client` do Git Bash, como no guia.
A app local (`:8050`) lê o secret com as credenciais do seu perfil AWS.

## Limpeza
```powershell
terraform destroy
```
O state guarda ARNs e nomes, mas nenhuma chave privada. Não versione `*.tfstate` nem `terraform.tfvars`.
