# cryptography-with-java

### Objective

[PT-BR]

Depois de muitas horas de pesquisa.
Coloquei neste repositório um curadoria de várias fontes de informação para que eu pudesse ter o código de rápido acesso para atender as necessidade para leitura de chave pública e privada para criptografia RSA e realizar a criptografia e descriptografia.

Utilizado AOP - Aspect Oriented Programming, ou Programação Orientada a Aspectos usando o spring-aspects, nos permite interceptar o request e response e realizar o tratamento necessário de forma abstrata e desacoplado do sistema.

Esta aplicação pode trabalhar tanto quanto criptografando dados enviando à outra aplicação (aplicação cliente), quanto ser a aplicação cliente que trata o dado e responde criptografado.

Se este repositório foi útil para você, não deixe de estrelar este conhecimento.

Ainda em construção.


---
[EN]

After many hours of research,
I have compiled in this repository a curation of various sources of information so that I could have quick access to the code to meet the needs for reading public and private keys for RSA encryption and perform encryption and decryption.

Using AOP - Aspect Oriented Programming with spring-aspects, allows us to intercept the request and response and perform the necessary processing in an abstract and decoupled manner from the system.

This application can work both by encrypting data (sending to another application/client application), as well as being a client application that processes the data and responds encrypted.

If this repository has been helpful to you, be sure to star this knowledge.

Under construction.

### How to Run
java -jar cryptography-with-java-0.0.1-SNAPSHOT.jar

nohup java -jar cryptography-with-java-0.0.1-SNAPSHOT.jar &

### CALLING API
curl --location --request POST 'localhost:8050/rest/criptografy'

---



# Creating Keys
Generating with OpenSSL

Ver good online tool: https://acte.ltd/utils/openssl

- Syntax: pkcs8
- Algorithm: RSA
- Size: 2048
- No Passphrase

### Private key

openssl genrsa -out private-dev.pem -aes256 2048

Senha: testdev123


### Public Key

openssl rsa -in private-dev.pem -pubout -out public-dev.pem



#### Testing

- Criptografando com a chave pública

openssl pkeyutl -in test.txt -encrypt -pubin -inkey public-dev.pem > encrypted-test.txt

- Descriptografando com a chave pública

openssl rsautl -decrypt -inkey private-dev.pem -in encrypted-test.txt > decrypted-test.txt


# Creating Keys - NO PASSPHRASE
Generating with OpenSSL


###  Private key

openssl genrsa -out private-nopwd-dev.pem 2048


   - Convert private Key to PKCS#8 format (so Java can read it)
   
openssl pkcs8 -topk8 -inform PEM -outform DER -in private-nopwd-dev.pem -out private-nopwd-dev.der -nocrypt


###  Public key

openssl rsa -in private-nopwd-dev.pem -pubout -out public-nopwd-dev.pem


   - Output public key portion in DER format (so Java can read it)
openssl rsa -in private-nopwd-dev.pem -pubout -outform DER -out public-nopwd-dev.der



#### Testing

- Criptografando com a chave pública

openssl pkeyutl -in test.txt -encrypt -pubin -inkey public-nopwd-dev.pem > encrypted-nopwd-test.txt

- Descriptografando com a chave pública

openssl rsautl -decrypt -inkey private-nopwd-dev.pem -in encrypted-nopwd-test.txt > decrypted-nopwd-test.txt


# Checking

- Check a private key

openssl rsa -in private-nopwd-dev.pem -check


- Check a public key

openssl rsa -inform PEM -pubin -in public-nopwd-dev.pem -text -noout

openssl pkey -inform PEM -pubin -in public-nopwd-dev.pem -text -noout


# Converting PEM to KEY file
(so Java can read it)


openssl rsa -outform der -in private-nopwd-dev.pem -out private-nopwd-dev.key

ssh-keygen -f private-nopwd-dev.pem > public-nopwd-dev.pub
