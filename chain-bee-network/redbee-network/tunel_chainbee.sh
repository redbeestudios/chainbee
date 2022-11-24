#!/usr/bin/env bash

LOCAL_PORTS=( 43330 43331 43332 43333 43334 43335 43336 43337 43338 )
FORMAT_CYAN="\e[34;1m"
FORMAT_YELLOW="\e[33m"
FORMAT_END="\e[0m"
FORMAT_RED="\e[31;1m"
FORMAT_GREEN="\e[32m"
FORMAT_BOLD="\e[1;4m"
FORMAT_LINK="\e]8;;"

function clean () {
  ### LIMPIAR TUNELES PREVIOS ###
echo -e "$FORMAT_YELLOW \n-------- Matando tuneles pre-existentes -------- $FORMAT_END\n\n"
for port in "${LOCAL_PORTS[@]}"; do
  $(kill $(lsof -ti tcp:$port) >/dev/null 2>&1)
  echo -e "- Matando proceso en puerto $port ... $FORMAT_END"
done
}

function home () {
  echo -e "$FORMAT_YELLOW \n--------- Conectando al /home de la vm --------- $FORMAT_END \n"
  ssh -i ~/.ssh/chainbee.pem ubuntu@54.87.108.161
}

function connect () {
  ### ABRIENDO TUNELES A VM_AWS ###
echo -e "$FORMAT_YELLOW \n--------------- Abriendo tuneles --------------- $FORMAT_END \n"
ssh -tfN -i ~/.ssh/chainbee.pem ubuntu@54.87.108.161 \
-L 43330:0.0.0.0:9443 \
-L 43331:0.0.0.0:7054 \
-L 43332:0.0.0.0:7050 \
-L 43333:0.0.0.0:5986 \
-L 43334:0.0.0.0:5984 \
-L 43335:0.0.0.0:5985 \
-L 43336:0.0.0.0:9051 \
-L 43337:0.0.0.0:7051 \
-L 43338:0.0.0.0:8051 -g
}

[[ $# == 0 ]] && { 
echo -e "$FORMAT_CYAN \n
|-------------------------------------------------------------------------| \r
| Tuneles chainbee aws                                                    | \r
| Script para levantar los tuneles necesarios.                            | \r
|                                                                         | \r
| Uso: ./tunel_chainbee.sh COMMAND                                        | \r
|   o: [sudo] ./tunel_chainbee.sh COMMAND                                 | \r
|                                                                         | \r
| Y COMMAND:                                                              | \r
|  - clean           limpiar todos los tuneles generados                  | \r
|  - aws             conecta a la VM de Aws                               | \r
|  - home            se conecta al /home de la vm                         | \r
|  para desconectarse del /home basta con escribir en la consola \"exit\"   | \r
| $FORMAT_GREEN EJ: ubuntu@Chain-Bees:~$ $FORMAT_YELLOW exit $FORMAT_CYAN                                        | \r
|-------------------------------------------------------------------------| \r
$FORMAT_END"; exit 1 ; }


if [[ $1 == "home" ]]; then
  home
  exit 1
elif [[ $1 == "clean" ]]; then
  clean
  exit 1
elif [[ $1 == "aws" ]]; then
  clean
  connect
  echo -e "\n$FORMAT_CYAN--------------- Entorno activado --------------- $FORMAT_END \n"
  echo -e "$FORMAT_BOLD Los siguientes servicios estan disponibles: $FORMAT_END \n
  $FORMAT_GREEN portainer$FORMAT_END: https://localhost:43330/ \r
  $FORMAT_GREEN ca.capitalHumano$FORMAT_END: http://localhost:43331/ \r 
  $FORMAT_GREEN orderer$FORMAT_END: http://localhost:43332/ \r
  $FORMAT_GREEN couchdb2$FORMAT_END: http://localhost:43333/_utils/ \r 
  $FORMAT_GREEN couchdb0$FORMAT_END: http://localhost:43334/_utils/ \r 
  $FORMAT_GREEN couchdb1$FORMAT_END: http://localhost:43335/_utils/ \r 
  $FORMAT_GREEN peer.management$FORMAT_END: http://localhost:43336/ \r
  $FORMAT_GREEN peer.capitalHumano$FORMAT_END: http://localhost:43337/ \r
  $FORMAT_GREEN peer.finanzas$FORMAT_END: http://localhost:43338/ \r \r

$FORMAT_CYAN------------ Credenciales portainer ------------  \r
  $FORMAT_YELLOW usr:$FORMAT_END: admin \r
  $FORMAT_YELLOW pw:$FORMAT_END: chainbee2022 \r
$FORMAT_CYAN------------- Credenciales CouchDB -------------  \r
  $FORMAT_YELLOW usr:$FORMAT_END: admin \r
  $FORMAT_YELLOW pw:$FORMAT_END: adminpw \r
  "
  exit 0
else
  echo -e "\n $FORMAT_RED ##### ERROR: Comando invalido ##### $FORMAT_END \n";
  exit 1
fi