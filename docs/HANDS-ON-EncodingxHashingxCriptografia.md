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

O certutil.exe é uma ferramenta de linha de comando nativa do Windows, instalada com os Serviços de Certificados, usada para gerenciar, exibir e configurar Autoridades de Certificação (AC),  permite verificar certificados e calcular hashes de arquivos. Porém não criptografa nem descriptografa arquivos diretamente.

E para tal usaremos o openssl.exe é uma ferramenta de linha de comando do OpenSSL, usada para criptografia, geração de chaves, certificados e muito mais.

`certutil` já vem no Windows. O `openssl` não vem, mas é instalado junto com o **Git for Windows**. Basta colocá-lo no PATH da sessão:

```cmd
set "PATH=%PATH%;C:\Program Files\Git\usr\bin"
openssl version

mkdir C:\hands-on
cd /d C:\hands-on

echo Pedido 1234 > mensagem.txt
cat mensagem.txt
```

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
Authorization: Basic UGVkaWRvIDEyMzQK
```

```cmd
echo UGVkaWRvIDEyMzQK| openssl base64 -d
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

### Casos de uso
Muito usado para guardar a senha do usuário no banco de dados. O hash é armazenado, e na hora do login o sistema calcula o hash da senha digitada e compara com o que está no banco. Se bater, a senha está correta. 

> Mensagem para gestão: para **senhas** use hash com *salt* e algoritmo lento (bcrypt, Argon2, PBKDF2), nunca SHA-256 puro nem MD5.
O 'SALT'  é um conjunto de dados aleatórios adicionados a uma informação (como uma senha ou um arquivo) antes de ela passar por uma função de hash.
O objetivo principal dele é garantir que dois dados iguais gerem resultados completamente diferentes

Curiosidade: Função 'hash' é usada para garantir a integridade da prova digital, na chamada Cadeia de Custódia como na entrega do laudo ou em auditorias/perícias ) para comprovar que o material digital examinado não foi alterado, como em casos de apreensão de celulares, computadores, pen drives, etc.

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

```
• openssl enc: Invoca a ferramenta de criptografia simétrica (encryption) do OpenSSL.
• -aes-256-cbc: Define o algoritmo de criptografia. Usa o AES com chave de 256 bits no modo de operação CBC (Cipher Block Chaining).
• -pbkdf2: Instrui o OpenSSL a usar o algoritmo PBKDF2 para transformar a sua senha em uma chave criptográfica segura (e um vetor de inicialização, o IV). Isso substitui o método antigo e inseguro (EVP_BytesToKey) que era padrão nas versões antigas do OpenSSL.
• -salt: Gera um salt (valor aleatório) antes de derivar a chave e o grava no cabeçalho do arquivo de saída. Isso garante que criptografar o mesmo texto duas vezes com a mesma senha gere arquivos completamente diferentes. (Nota: No OpenSSL moderno, o uso de salt já é padrão, mas mantê-lo explícito é uma boa prática).
• -in mensagem.txt: Especifica o arquivo de entrada (o texto original que você quer proteger).
• -out mensagem.aes: Especifica o nome do arquivo de saída que conterá os dados criptografados.
• -pass: Indica a fonte de onde o OpenSSL deve ler a senha. Atenção: No seu exemplo, o comando está incompleto no final, pois -pass exige um argumento logo em seguida.

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

## 4b. Assimétrica no mundo real: mTLS + chave no Secrets Manager

Pré-requisito: o setup em [AWS-MTLS-SECRETS-MANAGER.md](AWS-MTLS-SECRETS-MANAGER.md) (API Gateway, Secrets Manager e arquivos em `C:\hands-on\mtls`).

```cmd
cd /d C:\hands-on\mtls
set "HOST=mtls-demo.<seu-dominio>"
```

### Camada 1: mTLS ("quem é você?")

Sem certificado de cliente, o API Gateway recusa a conexão:

```cmd
(echo GET /hello HTTP/1.1& echo Host: %HOST%& echo Connection: close& echo.) | openssl s_client -quiet -connect %HOST%:443 -servername %HOST%
```

Com o certificado assinado pela CA que está no truststore:

```cmd
(echo GET /hello HTTP/1.1& echo Host: %HOST%& echo Connection: close& echo.) | openssl s_client -quiet -connect %HOST%:443 -servername %HOST% -cert client.pem -key client.key
```

Resultado: `HTTP/1.1 200 OK` e `{"status":"ok","mensagem":"mTLS validado"}`.

> Usamos `openssl s_client` porque o `curl` do Windows (nativo e do Git) usa Schannel, que não aceita certificado de cliente em PEM.

**O que mostrar:** sem o certificado certo, a conexão nem chega na API. A assimetria está no certificado: o cliente prova que tem a chave privada (`client.key`) sem nunca enviá-la.

### Camada 2: o dado protegido, com a chave no cofre

Suba a app (porta 8050) com o perfil AWS configurado e cifre o dado com a chave **pública** da app:

```cmd
openssl pkeyutl -encrypt -pubin -inkey app-publica.pem -pkeyopt rsa_padding_mode:oaep -pkeyopt rsa_oaep_md:sha256 -pkeyopt rsa_mgf1_md:sha256 -in ..\mensagem.txt -out msg.rsa
openssl base64 -A -in msg.rsa -out msg.b64
type msg.b64

for /f %i in (msg.b64) do echo {"field":"%i"}> body.json

curl -s -H "Content-Type: application/json" --data @body.json http://localhost:8050/secretsdemo/decrypt
```

Resultado: `{"plaintext":"Pedido 1234 - valor R$ 500,00\r\n"}`. O `\r\n` é a quebra de linha que o `echo` gravou no `mensagem.txt`.

**O que mostrar:**
- Abra `src/main/resources/secretsdemo.properties`: **não tem chave privada**, só o nome do secret (`hands-on/rsa-private-key`).
- Abra o secret no console da AWS: a chave está lá, protegida por IAM, e cada leitura fica auditada no CloudTrail.
- Rode a app **sem** credencial AWS: o endpoint volta `502 Falha ao obter a chave no Secrets Manager`. Sem permissão, não há chave.
- Contraste: o `application.properties` antigo tinha a chave privada em texto claro, e no Git. **Antes: chave no Git → Depois: chave no cofre.**

> Em script `.cmd`, troque `%i` por `%%i` no `for /f`.

---

## Limpeza

```cmd
cd /d C:\
rmdir /s /q C:\hands-on
```
