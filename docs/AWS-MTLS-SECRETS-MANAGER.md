# Setup AWS: mTLS no API Gateway + chave privada no Secrets Manager

Preparação **antes** da apresentação para a seção 4 do [hands-on](HANDS-ON-EncodingxHashingxCriptografia.md). Faça isso numa conta AWS de **dev/sandbox**, nunca em produção.

```
              (1) mTLS: quem é você?                       (2) Payload: o dado em si
cmd ──[client.pem]──► API Gateway (custom domain)          cmd ──[campo cifrado c/ chave PÚBLICA]──► app local :8050
                         │ valida cert contra truststore (S3)                                          │
                         └─► Mock 200                                         Secrets Manager ◄──────┘ busca chave PRIVADA
```

- **mTLS** autentica o **cliente** e cifra o **canal**. O TLS termina no API Gateway.
- **Payload** protege o **dado**: só quem tem a chave privada (guardada no Secrets Manager) lê.
- São **pares de chaves diferentes**.

---

## Parte A: Material criptográfico (cmd)

Gere tudo **fora do repositório**. Nada aqui vai para o Git.

```cmd
set "PATH=%PATH%;C:\Program Files\Git\usr\bin"
mkdir C:\hands-on\mtls
cd /d C:\hands-on\mtls
```

**CA de demonstração** (quem "assina" os clientes confiáveis):

```cmd
openssl req -x509 -newkey rsa:2048 -nodes -keyout ca.key -out ca.pem -days 30 -subj "/CN=Demo Workshop CA"
copy ca.pem truststore.pem
```

> **Erro `name is expected to be in the format /type0=value0/...` com `C:/Program Files/Git/CN=...`?**
> Você rodou no **Git Bash**, que converte argumentos iniciados em `/` em caminhos do Windows. Os comandos acima são para o **cmd**. No Git Bash, use um destes:
> - `MSYS_NO_PATHCONV=1 openssl req ... -subj "/CN=Demo Workshop CA"`
> - ou barra dupla: `-subj "//CN=Demo Workshop CA"`
>
> Vale também para o `-subj` do certificado do cliente. No Git Bash, troque `copy` por `cp` e `set PATH` não é necessário.

**Certificado do cliente**, assinado pela CA:

```cmd
openssl req -newkey rsa:2048 -nodes -keyout client.key -out client.csr -subj "/CN=cliente-demo"
openssl x509 -req -in client.csr -CA ca.pem -CAkey ca.key -CAcreateserial -out client.pem -days 30
```

**Par RSA do payload.** É um par **novo**: não reutilize as chaves que estão no `application.properties`, porque elas estão no histórico do Git.

```cmd
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out app-privada.pem
openssl pkey -in app-privada.pem -pubout -out app-publica.pem
```

| Arquivo | Vai para | Secreto? |
|---|---|---|
| `truststore.pem` (= `ca.pem`) | Bucket S3 do API Gateway | Não |
| `ca.key` | Fica guardado offline | **Sim** |
| `client.pem` + `client.key` | Máquina do cliente (o cmd da demo) | `client.key` **sim** |
| `app-publica.pem` | Quem vai enviar dados (o cmd da demo) | Não |
| `app-privada.pem` | **Secrets Manager**. Depois apague o arquivo local | **Sim** |

---

## Parte B: Console AWS

Use a **mesma região** em todos os passos (ex.: `us-east-1`) e a mesma no `src/main/resources/secretsdemo.properties`.

### 1. S3: truststore
1. Crie um bucket **privado** (Block Public Access ligado) com **versionamento** habilitado.
2. Faça upload de `truststore.pem`.
3. Anote a URI: `s3://<bucket>/truststore.pem`.

### 2. ACM: certificado do domínio
1. ACM → Request a public certificate → `mtls-demo.<seu-dominio>`.
2. Validação por **DNS** → "Create records in Route 53".
3. Aguarde o status *Issued*.

### 3. API Gateway: API com Mock 200
1. API Gateway → Create API → **REST API** (Regional). Use REST porque a HTTP API não tem integração Mock.
2. Crie o recurso `/hello` e o método `GET`, com integração do tipo **Mock**.
3. Em *Integration response* → 200 → Mapping templates → `application/json`:
   ```json
   {"status":"ok","mensagem":"mTLS validado"}
   ```
4. Deploy API → novo stage `demo`.

### 4. Custom domain com mTLS
1. API Gateway → Custom domain names → Create:
   - Domain: `mtls-demo.<seu-dominio>`, endpoint **Regional**, TLS **1.2**
   - Certificado ACM do passo 2
   - **Mutual TLS authentication: ligado** → Truststore URI: `s3://<bucket>/truststore.pem`
2. Aba *API mappings* → mapeie a API e o stage `demo` (path vazio).
3. Anote o **API Gateway domain name** (`d-xxxx.execute-api.<regiao>.amazonaws.com`).

### 5. Desabilitar o endpoint padrão (importante)
Na API → *API settings* → **Default endpoint: Disabled** → faça o deploy de novo.
Sem isso, a URL `https://<id>.execute-api...` continua aberta **sem mTLS** e qualquer um contorna o controle.

### 6. Route 53
Hosted zone → Create record → `mtls-demo` → tipo **A** → **Alias** → *API Gateway API* → região → o domínio do passo 4.

### 7. Secrets Manager: chave privada
1. Store a new secret → **Other type of secret** → aba **Plaintext**.
2. Cole o conteúdo inteiro de `app-privada.pem`, incluindo as linhas `-----BEGIN PRIVATE KEY-----` e `-----END PRIVATE KEY-----`.
3. Nome: `hands-on/rsa-private-key`.
4. Depois de criado, **apague** o `app-privada.pem` local: `del app-privada.pem`.

### 8. IAM: permissão mínima para a app local
Anexe ao usuário ou role do seu perfil local:

```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Action": "secretsmanager:GetSecretValue",
    "Resource": "arn:aws:secretsmanager:<regiao>:<conta>:secret:hands-on/rsa-private-key-*"
  }]
}
```

Configure as credenciais locais com `aws configure` ou `aws sso login --profile <perfil>` (e `set AWS_PROFILE=<perfil>`). A app usa a cadeia padrão de credenciais do SDK.

---

## Checklist de ensaio
- [ ] `openssl s_client` **sem** certificado → recusado
- [ ] `openssl s_client` **com** certificado → `HTTP/1.1 200` + `{"status":"ok",...}`
- [ ] URL `execute-api` padrão → não responde (endpoint desabilitado)
- [ ] App local: `POST /secretsdemo/decrypt` devolve o texto claro
- [ ] App sem credencial AWS → `502` com mensagem clara (bom para mostrar que sem IAM não há chave)

## Limpeza pós-apresentação
1. Route 53: apague o registro `mtls-demo`.
2. API Gateway: apague o custom domain e a API.
3. ACM: apague o certificado.
4. S3: esvazie (todas as versões) e apague o bucket.
5. Secrets Manager: apague o secret (janela de recuperação de 7 dias, ou pela CLI: `aws secretsmanager delete-secret --secret-id hands-on/rsa-private-key --force-delete-without-recovery`).
6. IAM: remova a política.
7. Local: `rmdir /s /q C:\hands-on\mtls`.
