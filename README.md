<p align="center">
    <img src="https://cdn-icons-png.flaticon.com/512/2818/2818233.png" alt="Logo" width="80" height="80">
</p>

# <h1 align="center">MyCloud</h1>
<h4 align="center">Projeto para a cadeira de Segurança Informática (Parte1) (2022/2023)</h4>

<hr>

# Objetivo
A primeira fase do trabalho tem como objetivo fundamental a construção de uma aplicação distribuída. O trabalho consiste na concretização de um sistema simplificado de armazenamento de ficheiros, designado por myCloud, onde o utilizador usa um servidor central para armazenar os seus ficheiros. 

<hr>

# Arquitectura do Sistema

O trabalho consiste no desenvolvimento de dois programas:
* O servidor myCloudServer
* A aplicação cliente myCloud que acede ao servidor via sockets TCP

A aplicação é distribuída de forma que o servidor fica numa máquina e o utilizador pode usar clientes em máquinas diferentes na Internet. 

# Instruções   
**1ºPasso: Ir para a pasta src/**

**2ºPasso: Correr o servidor**

```bash
java myCloudServer 'PortNumber'
```
**3ºPasso: Correr o cliente**

```bash
java myCloud -a 'HOST':'PortNumber' {-c||-s||-e||-g} {<filenames>}+ 
```
**-a 'HOST':'PortNumber'**

Identifica o servidor (hostname ou endereço IP e porto; por exemplo 127.0.0.1:23456). 

**-c filames+**

O cliente cifra um ou mais ficheiros e envia-os para o servidor. <br>
Caso algum dos ficheiros já exista no servidor ou caso algum dos ficheiros não exista localmente, apresenta uma mensagem de erro aoutilizador e continua para os seguintes ficheiros. <br>
O cliente usa cifras híbridas. Assim, a chave usada para cifrar cada ficheiro é cifrada no cliente e enviada para o servidor. Cada uma destas chaves pode ser guardada num ficheiro cujo nome deve ser o nome do ficheiro original com extensão chave_secreta. <br>
Os ficheiros cifrados são guardados no servidor com extensão cifrado. <br> 

Exemplo: 
```bash
java myCloud -a 127.0.0.1:23456 -c trab1.pdf aulas.doc
```

**-s filenames+** 

O cliente assina um ou mais ficheiros e envia-os para o servidor. <br>
Caso algum dos ficheiros já exista no servidor ou caso algum dos ficheiros não exista localmente, apresenta uma mensagem de erro ao
utilizador e continua para os seguintes.<br>
As assinaturas devem são guardadas separadamente em ficheiros com extensão assinatura. Os ficheiros assinados são guardados no servidor com extensão
assinado. 

Exemplo: 
```bash
java myCloud -a 127.0.0.1:23456 -s trab1.pdf aulas.doc
```

**-e filenames+**

O cliente assina e cifra um ou mais ficheiros e envia-os para o servidor. <br>
Caso algum dos ficheiros já exista no servidor ou caso algum dos ficheiros não exista localmente, apresenta uma mensagem de
erro ao utilizador e continua para os seguintes. <br>
O cliente usa envelopes seguros portanto os ficheiros são guardados no servidor com extensão seguro.

Exemplo: 
```bash
java myCloud -a 127.0.0.1:23456 -e trab1.pdf aulas.doc
```

**-g filenames+**

O cliente recebe um ou mais ficheiros. <br> 
Caso algum dos ficheiros já exista localmente ou caso algum dos ficheiros não exista no servidor, apresenta uma mensagem de erro ao utilizador e continua para os seguintes.
O cliente decifra os ficheiros que tenham sido cifrados.
O cliente verifica a assinatura dos ficheiros que tenham sido assinados.

Exemplo: 
```bash
java myCloud -a 127.0.0.1:23456 -g trab1.pdf aulas.doc
```
#Notas 
Toda criptografia assimétrica no trabalho é com RSA com chaves de 2048 bits. <br>
A criptografia simétrica é efetuada com AES e chaves de 128 bits. <br>
Os utilizadores do myCloud têm de ter um par de chaves na keystore designada por keystore.??Cloud














