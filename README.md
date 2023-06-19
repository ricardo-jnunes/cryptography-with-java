# cryptography-with-java

### Objective

Depois de muitas horas de pesquisa.
Coloquei neste repositório um curadoria de várias fontes de informação para que eu pudesse ter o código de rápido acesso para atender as necessidade para leitura de chave pública e privada para criptografia RSA e realizar a criptografia e descriptografia.

Ainda em construção.



After many hours of research.
I put in this repository a curation of several sources of information so that I could have the quick access code to meet the need to read the public and private key for RSA encryption and perform the encryption and decryption.

Under construction.

### How to Run
java -jar cryptography-with-java-0.0.1-SNAPSHOT.jar

nohup java -jar cryptography-with-java-0.0.1-SNAPSHOT.jar &

### CALLING API
curl --location --request POST 'localhost:8050/rest/criptografy'

---



# Creating Keys
Generating with OpenSSL


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
