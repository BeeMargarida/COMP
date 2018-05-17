# COMP

#### Group 31

Para compilar os ficheiros pode-se correr o script criado, no terminal Linux, escrevendo o seguinte comando dentro da pasta do Project/src: 

./script.sh

Após isso a compilação estará concluída, e será possivel executar o parser com um comando semelhante a:

java Parser YAL\ Files/<nomeDoFicheiro>.yal

Por agora, o código gerado encontra-se no ficheiro code_generated.j, no diretório Project/src.

## Checkpoint 2

Para este checkpoint alguns erros do checkpoint anterior foram resolvidos. A análise semântica está completa exceto a chamada de funções em estruturas condicionais. 
A geração do código trata da estrutura principal dos módulos e funções, assim como funções aritméticas e chamamento de funções. A parte relativamente ao ".limit" da stack ainda não está implementada.