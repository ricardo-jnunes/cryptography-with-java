# Commands to Generate Certificates

#### Algoritmo Assimétrico RSA

### Creating Keys
Generating with OpenSSL

Ver good online tool: https://acte.ltd/utils/openssl

- Syntax: pkcs8
- Algorithm: RSA
- Size: 2048
- No Passphrase

#### Private key

openssl genrsa -out private-dev.pem -aes256 2048

Senha: testdev123


#### Public Key

openssl rsa -in private-dev.pem -pubout -out public-dev.pem



##### Testing

- Criptografando com a chave pública

openssl pkeyutl -in test.txt -encrypt -pubin -inkey public-dev.pem > encrypted-test.txt

- Descriptografando com a chave pública

openssl rsautl -decrypt -inkey private-dev.pem -in encrypted-test.txt > decrypted-test.txt


### Creating Keys - NO PASSPHRASE
Generating with OpenSSL


####  Private key

openssl genrsa -out private-nopwd-dev.pem 2048


   - Convert private Key to PKCS#8 format (so Java can read it)
   
openssl pkcs8 -topk8 -inform PEM -outform DER -in private-nopwd-dev.pem -out private-nopwd-dev.der -nocrypt


####  Public key

openssl rsa -in private-nopwd-dev.pem -pubout -out public-nopwd-dev.pem


   - Output public key portion in DER format (so Java can read it)
openssl rsa -in private-nopwd-dev.pem -pubout -outform DER -out public-nopwd-dev.der



##### Testing

- Criptografando com a chave pública

openssl pkeyutl -in test.txt -encrypt -pubin -inkey public-nopwd-dev.pem > encrypted-nopwd-test.txt

- Descriptografando com a chave pública

openssl rsautl -decrypt -inkey private-nopwd-dev.pem -in encrypted-nopwd-test.txt > decrypted-nopwd-test.txt


### Checking

- Check a private key

openssl rsa -in private-nopwd-dev.pem -check


- Check a public key

openssl rsa -inform PEM -pubin -in public-nopwd-dev.pem -text -noout

openssl pkey -inform PEM -pubin -in public-nopwd-dev.pem -text -noout


### Converting PEM to KEY file
(so Java can read it)


openssl rsa -outform der -in private-nopwd-dev.pem -out private-nopwd-dev.key

ssh-keygen -f private-nopwd-dev.pem > public-nopwd-dev.pub
