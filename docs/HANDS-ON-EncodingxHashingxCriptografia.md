# Hands-on: Encoding x Hashing x Criptografia

Roteiro para rodar ao vivo no **cmd do Windows**. São 4 demos: Encoding, Hashing, Criptografia Simétrica (AES) e Criptografia Assimétrica (RSA).

| Conceito | Precisa de chave? | Volta ao original? | Para que serve |
|---|---|---|---|
| Encoding (Base64) | Não | Sim, qualquer pessoa consegue | Transportar dados (formato), **não protege nada** |
| Hashing (SHA-256) | Não | Não (é de mão única) | Integridade, "impressão digital" do dado |
| Simétrica (AES) | Sim, **uma** chave compartilhada | Sim, só quem tem a chave | Proteger dados em repouso ou em trânsito |
| Assimétrica (RSA) | Sim, **par** pública/privada | Sim, só quem tem a chave privada | Troca de chaves, assinatura digital, identidade |

---

## 0. Preparação (antes da apresentação)

`certutil` já vem no Windows. O `openssl` não vem, mas é instalado junto com o **Git for Windows**. Basta colocá-lo no PATH da sessão:

```cmd
set "PATH=%PATH%;C:\Program Files\Git\usr\bin"
openssl version

mkdir C:\hands-on
cd /d C:\hands-on

echo Pedido 1234 - valor R$ 500,00> mensagem.txt
type mensagem.txt
```

> Dica: não deixe espaço antes do `>` no `echo`, senão o arquivo ganha um espaço no final e o hash muda.

---

## 1. Encoding (Base64)

```cmd
certutil -f -encode mensagem.txt mensagem.b64
type mensagem.b64

certutil -f -decode mensagem.b64 decodificado.txt
type decodificado.txt
```

**O que mostrar:** o texto parece "embaralhado", mas voltou ao original **sem senha nem chave**. Base64 não é segurança. É só uma forma de representar o dado. (O `certutil` coloca um cabeçalho `BEGIN CERTIFICATE` por padrão. Ignore, é só a moldura.)

Alternativa com openssl (sem o cabeçalho):

```cmd
openssl base64 -in mensagem.txt
```

### 1b. "Ataque": revelar uma senha em 1 comando

Muita gente acha que Base64 "esconde" credenciais. Mostre dois casos reais:

**Caso 1: header HTTP Basic Auth** capturado em um log ou proxy:

```text
Authorization: Basic YWRtaW46U2VuaGFAMTIz
```

```cmd
echo YWRtaW46U2VuaGFAMTIz| openssl base64 -d
```

Resultado: `admin:Senha@123` (usuário e senha em texto claro).

**Caso 2: Secret do Kubernetes**, que guarda os valores só em Base64 no YAML:

```yaml
data:
  db-password: U2VuaGFEb0JhbmNvMjAyNg==
```

```cmd
echo U2VuaGFEb0JhbmNvMjAyNg==| openssl base64 -d
```

Resultado: `SenhaDoBanco2026`.

Versão só com ferramenta nativa do Windows (sem openssl):

```cmd
echo YWRtaW46U2VuaGFAMTIz> header.b64
certutil -f -decode header.b64 header.txt
type header.txt
```

**Mensagem para a plateia:** Base64 não tem chave, então **qualquer pessoa** reverte em segundos, até em sites públicos de "decode". Credencial em Base64 é credencial exposta. Por isso o Basic Auth exige HTTPS e Secrets precisam de criptografia em repouso ou de um cofre (Key Vault, Secrets Manager).

---

## 2. Hashing (SHA-256)

```cmd
certutil -hashfile mensagem.txt SHA256

echo Pedido 1234 - valor R$ 900,00> adulterada.txt
certutil -hashfile adulterada.txt SHA256

certutil -hashfile mensagem.txt SHA256
```

**O que mostrar:**
- Trocar **um único caractere** (500 → 900) gera um hash completamente diferente (efeito avalanche). É assim que se detecta adulteração.
- Rodar de novo no mesmo arquivo dá **sempre o mesmo hash** (determinístico).
- Não existe comando para "des-hashear": é de mão única.

> Mensagem para gestão: para **senhas** use hash com *salt* e algoritmo lento (bcrypt, Argon2, PBKDF2), nunca SHA-256 puro nem MD5.

---

## 3. Criptografia Simétrica (AES-256)

Uma única chave (derivada da senha) cifra e decifra.

```cmd
openssl enc -aes-256-cbc -pbkdf2 -salt -in mensagem.txt -out mensagem.aes -pass pass:Senha@Demo2026
type mensagem.aes

openssl enc -d -aes-256-cbc -pbkdf2 -in mensagem.aes -out decifrada-aes.txt -pass pass:Senha@Demo2026
type decifrada-aes.txt

openssl enc -d -aes-256-cbc -pbkdf2 -in mensagem.aes -out falha.txt -pass pass:SenhaErrada
```

**O que mostrar:**
- `type mensagem.aes` mostra lixo binário (começa com `Salted__`): o conteúdo está protegido.
- Com a senha certa, o texto volta. Com a senha errada aparece `bad decrypt`.
- O problema da simétrica: **como entregar a chave ao outro lado com segurança?** Isso leva à próxima demo.

> Em produção (ex.: Java), prefira **AES-GCM**, que também garante integridade. O `openssl enc` não suporta GCM, por isso a demo usa CBC.

---

## 4. Criptografia Assimétrica (RSA-2048)

Par de chaves: a **pública** pode ser distribuída e cifra. A **privada** fica em segredo e decifra.

```cmd
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out privada.pem
openssl pkey -in privada.pem -pubout -out publica.pem
type publica.pem

openssl pkeyutl -encrypt -pubin -inkey publica.pem -pkeyopt rsa_padding_mode:oaep -in mensagem.txt -out mensagem.rsa
type mensagem.rsa

openssl pkeyutl -decrypt -inkey privada.pem -pkeyopt rsa_padding_mode:oaep -in mensagem.rsa -out decifrada-rsa.txt
type decifrada-rsa.txt

openssl pkeyutl -decrypt -pubin -inkey publica.pem -pkeyopt rsa_padding_mode:oaep -in mensagem.rsa
```

**O que mostrar:**
- A chave pública pode ser exibida para a plateia. Ela **não** decifra (o último comando falha com `A private key is needed for this operation`).
- Só quem tem `privada.pem` lê a mensagem. Por isso ela vai para um cofre (Key Vault / Secrets Manager), nunca para o Git.
- Na vida real (TLS/HTTPS) os dois se combinam: RSA/ECDH troca uma chave AES, e o AES cifra os dados, porque é muito mais rápido.

---

## Limpeza

```cmd
cd /d C:\
rmdir /s /q C:\hands-on
```
