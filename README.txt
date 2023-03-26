
Como executar o programa: 

1º: Ir para a pasta src/ 

Caso para correr o servidor: 
	-> java myCloudServer 'PortNumber'

Caso para correr o cliente: 
	-> java myCloud -a 'HOST':'PortNumber' {-c||-s||-e||-g} {<filenames>}+ 

Para geral o executáveis, se necessário 
	
	-> javac myCloudServer.java
	-> javac myCloud.java

Notas:

Os ficheiros recebidos pelo server através dos comandos {-c||-s||-e} irão para a pasta cloud.
	- cifrados, assinados, seguros -> cloud/files
	- chaves secretas -> cloud/keys
	- assinaturas -> cloud/signatures

Os ficheiros recebidos pelo cliente através do comando -g irão para a pasta receivedFiles.