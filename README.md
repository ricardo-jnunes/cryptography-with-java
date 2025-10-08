# cryptography-with-java

### Objective
Casos de uso com RSA e mTLS com ECDSA
#### Algoritmo RSA

O algoritmo de criptografia Rivest-Shamir-Adleman (RSA) foi o primeiro algoritmo de criptografia assimétrica amplamente adotado.
A criptografia assimétrica funciona por meio da criação de um par de chaves, uma pública e outra privada. 

Qualquer pessoa pode usar uma chave pública para criptografar dados. No entanto, apenas os detentores da chave privada correspondente podem descriptografar esses dados.
Porém, com o RSA, tanto a chave privada quanto a pública podem criptografar os dados, enquanto a outra chave os descriptografa.
Mas o verdadeiro propósito desse tipo de criptografia é provar a identidade do remetente. Se os dados fossem roubados e modificados em trânsito, a chave pública não seria capaz de descriptografar a nova mensagem, e assim o destinatário saberia que os dados foram modificados em trânsito.

##### Como funciona?

A criptografia assimétrica mantém a segurança dos dados usando algoritmos criptográficos para gerar um par de chaves: uma pública e uma privada. Qualquer pessoa pode usar uma chave pública para criptografar dados, mas apenas as pessoas com a chave privada correta podem descriptografá-los e lê-los.

- A chave pública criptografa dados ou verifica assinaturas digitais e pode ser distribuída e compartilhada livremente.
- A chave privada descriptografa dados e cria assinaturas digitais, mas deve ser secreta para garantir a segurança.

Com o RSA, tanto a chave privada quanto a pública podem criptografar os dados, enquanto a outra chave os descriptografa.
Mas o verdadeiro propósito desse tipo de criptografia é provar a identidade do remetente. Se os dados fossem roubados e modificados em trânsito, a chave pública não seria capaz de descriptografar a nova mensagem, e assim o destinatário saberia que os dados foram modificados em trânsito.

##### Por que usar?
A segurança da criptografia da chave pública depende de manter a chave privada confidencial e, ao mesmo tempo, compartilhar livremente a chave pública. A chave pública só pode criptografar dados, dessa forma não é muito interessante para os agentes de ameaças. Como os usuários nunca precisam compartilhar suas chaves privadas, isso reduz o risco de hackers interceptarem essas chaves que são muito mais valiosas.

##### Implementando criptografia com Algoritmo RSA
Utilizado AOP - Aspect Oriented Programming, ou Programação Orientada a Aspectos usando o spring-aspects, nos permite interceptar o request e response e realizar o tratamento necessário de forma abstrata e desacoplado do sistema.

Esta aplicação pode trabalhar tanto quanto criptografando dados enviando à outra aplicação (aplicação cliente), quanto ser a aplicação cliente que trata o dado e responde criptografado.


- Suporte Nativo Java:
Você precisa converter sua chave privada para o formato PKCS8 para que o Java possa ler:
	- PKCS#8 PEM format: O Java Development Kit (JDK) oferece suporte nativo ao carregamento de chaves privadas codificadas no formato PKCS#8 PEM (por exemplo, BEGIN PRIVATE KEY, BEGIN ENCRYPTED PRIVATE KEY);
	- X.509 PEM para chaves públicas e certificados: o Java também pode carregar facilmente chaves públicas e certificados de arquivos PEM que seguem o padrão X.509 (por exemplo, BEGIN PUBLIC KEY, BEGIN CERTIFICATE).


---

### How to Run
java -jar cryptography-with-java-0.0.1-SNAPSHOT.jar

nohup java -jar cryptography-with-java-0.0.1-SNAPSHOT.jar &

#### CALLING API
curl --location --request POST 'localhost:8050/criptografy/rsa/encrypt'

---

### Support
Depois de muitas horas de pesquisa.
Coloquei neste repositório um curadoria de várias fontes de informação para que eu pudesse ter o código de rápido acesso para atender as necessidade para leitura de chave pública e privada para criptografia RSA e realizar a criptografia e descriptografia.

Também adicionamos o caso de uso mTLS - Como implementar no Java a comunicação por mTLS.

Se este repositório foi útil para você, não deixe de estrelar este conhecimento.

Em construção.