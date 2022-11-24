# Chainbee Project

------------
## Pre-Requisitos

------------

- Docker
- Golang  >=1.18
- Java 11
- curl

## Tools

------------

[Portainer](http://https://docs.portainer.io/start/install/server/docker/linux "Portainer"): Portainer es una gui para docker.

#### How to install
1. docker volume create portainer_data
2. docker run -d -p 8000:8000 -p 9443:9443 —name portainer —restart=always -v /var/run/docker.sock:/var/run/docker.sock -v portainer_data:/data portainer/portainer-ce:latest
3.  ingresar a https://localhost:9443 y crearse un usuario y password, hacerlo dentro de los 5 minutos de haber levantado el contenedor


------------


## Hyperledger Fabric

------------

Hyperledger Fabric es una solución open source que nos permite implementar DLTs (distributed ledger technology) permisionadas, consta de un conjunto de instrucciones y contenedores de docker (peers, orderers, ca y tool), las cuales nos van a permitir configurar, mantener y transaccionar en nuestra red. Para mas informacion  [Link](https://hyperledger-fabric.readthedocs.io/en/latest/whatis.html "Link")

#### How to install

	export FABRICSamplesDir="$HOME/hyperledger/fabric"
	mkdir -p $FABRICSamplesDir

	sudo chmod -R 777 $FABRICSamplesDir
	cd $FABRICSamplesDir
	sudo curl -sSL http://bit.ly/2ysbOFE | bash -s 2.4.7

	export PATH=$PATH:$HOME/hyperledger/fabric/fabric-samples/bin
	source ~/.profile
	source $HOME/.profile

------------

## Creación de la red

------------

Clonarse el repo e ingresar a [redbee-network](chain-bee-network/redbee-network)

#### Creamos el material material criptografico

------------

Con material criptografico nos referimos los certificados de confianza (PKI) que vamos a utilizar para autentificarnos en la red. Tambien incluye los certificados tls.

**¡importante!**, a los fines de levantar una red de desarrollo relativamente rapido vamosa utilizar **cryptogen** comando proporcionado por Fabric, no debe utilizarse en ambientes productivos ya que genera certificados estaticos.  Para producción fabric nos proporciona un CA Authority  [FABRIC CA](http:https://hyperledger-fabric-ca.readthedocs.io/en/latest/// "FABRIC CA")

	export CHANNEL_NAME=chainbee
	export VERBOSE=false
	export FABRIC_CFG_PATH=$PWD

	cryptogen generate --config=./crypto-config.yaml

#### Bloque Genesis y Canal

Como su nombre lo indica es el bloque de la cadena, inicializando la configuración del orderer.  dicha configuracion se encuentra en **configtx.yaml**

	configtxgen  -profile ThreeOrgsOrdererGenesis -channelID system-channel -outputBlock ./channel-artifacts/genesis.block

	configtxgen -profile ThreeOrgsChannel -outputCreateChannelTx ./channel-artifacts/channel.tx -channelID $CHANNEL_NAME

#### Anchors peers  y msp

Si bien cada organización internamente puede contar con Multiples peers... a la red se expone solo uno a este peer se llama Anchor peers, esta configuracion tambien se encuentra dentro **configtx.yaml**.  Junto con anchor peer viene otro concepto que es el de Msp (membership service provider) esta configuracion nos permite configurar los niveles de acceso de los peers y clientes.

	configtxgen -profile ThreeOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/CapitalHumanoMSPanchors.tx -channelID $CHANNEL_NAME -asOrg CapitalHumanoMSP

	configtxgen -profile ThreeOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/FinanzasMSPanchors.tx -channelID $CHANNEL_NAME -asOrg FinanzasMSP

	configtxgen -profile ThreeOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/ManagementMSPanchors.tx -channelID $CHANNEL_NAME -asOrg ManagementMSP

## Levanta peers, orderer, ca, cli, couchdb
	CHANNEL_NAME=$CHANNEL_NAME docker-compose -f docker-compose-cli-couchdb.yaml up -d

¿Que Levantamos?
- Orderer: el orderer se va a encargar de ordenar los bloques que van llegando y ejecutar los chaincode (smart contract). Para este caso solo se utiliza 1 con un protocolo de consenso llamado **solo**. En futuras iteraciones se van a utilizar  uno por organizacion con protocolo **[Raft](https://raft.github.io/ "Raft")**
- Peers: cada miembro del consorcio, quienes van a transaccionar en la red
- couchdb
- Fabric Ca
- Cli: una tool que nos permite intercatuar con la red

Listo.. tenemos una red Levantada!

## Crear Canal

Entrar a la consola de la cli

	export CHANNEL_NAME=chainbee

	peer channel create -o orderer.redbee.com:7050 -c $CHANNEL_NAME -f ./channel-artifacts/channel.tx --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/redbee.com/msp/tlscacerts/tlsca.redbee.com-cert.pem


## Unirse al canal

	peer channel join -b chainbee.block

	CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/users/Admin@management.redbee.com/msp/ CORE_PEER_ADDRESS=peer0.management.redbee.com:7051 CORE_PEER_LOCALMSPID="ManagementMSP" CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/peers/peer0.management.redbee.com/tls/ca.crt peer channel join -b chainbee.block

	CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/finanzas.redbee.com/users/Admin@finanzas.redbee.com/msp/ CORE_PEER_ADDRESS=peer0.finanzas.redbee.com:7051 CORE_PEER_LOCALMSPID="FinanzasMSP" CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/finanzas.redbee.com/peers/peer0.finanzas.redbee.com/tls/ca.crt peer channel join -b chainbee.block


## Agregamos los anchors peers al Canal

	peer channel update -o orderer.redbee.com:7050 -c $CHANNEL_NAME -f ./channel-artifacts/CapitalHumanoMSPanchors.tx --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/redbee.com/msp/tlscacerts/tlsca.redbee.com-cert.pem

	CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/users/Admin@management.redbee.com/msp/ CORE_PEER_ADDRESS=peer0.management.redbee.com:7051 CORE_PEER_LOCALMSPID="ManagementMSP" CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/peers/peer0.management.redbee.com/tls/ca.crt  peer channel update -o orderer.redbee.com:7050 -c $CHANNEL_NAME -f ./channel-artifacts/ManagementMSPanchors.tx --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/redbee.com/msp/tlscacerts/tlsca.redbee.com-cert.pem

	CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/finanzas.redbee.com/users/Admin@finanzas.redbee.com/msp/ CORE_PEER_ADDRESS=peer0.finanzas.redbee.com:7051 CORE_PEER_LOCALMSPID="FinanzasMSP" CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/finanzas.redbee.com/peers/peer0.finanzas.redbee.com/tls/ca.crt peer channel update -o orderer.redbee.com:7050 -c $CHANNEL_NAME -f ./channel-artifacts/FinanzasMSPanchors.tx --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/redbee.com/msp/tlscacerts/tlsca.redbee.com-cert.pem



## Chaincode Java
### Build

Generamos el build de chaincode/BeeManager

	./gradlew installDist (hacerlo por fuera de la cli y el resultado moverlo a redbee-network)

El build generado se encuentra en: build/install/java-bee-manager
Luego

	export CHANNEL_NAME=chainbee && export CHAINCODE_NAME=java-bee-manager && export CHAINCODE_VERSION=1.1 && export CC_RUNTIME_LANGUAGE=java && export CC_SRC_PATH="../../../chaincode/BeeManager/build/install/java-bee-manager" && export ORDERER_CA=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/redbee.com/msp/tlscacerts/tlsca.redbee.com-cert.pem



## Chaincode Go
En caso de contar con un chaincode en Golang solo basta con :

	export CHANNEL_NAME=chainbee
	export CHAINCODE_NAME=foodcontrol
	export CHAINCODE_VERSION=1
	export CC_RUNTIME_LANGUAGE=golang
	export CC_SRC_PATH="../../../chaincode/$CHAINCODE_NAME/"
	export ORDERER_CA=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/redbee.com/msp/tlscacerts/tlsca.redbee.com-cert.pem


## empaqueta el chaincode
Tanto sea un chaincode Java o Go

	peer lifecycle chaincode package ${CHAINCODE_NAME}.tar.gz --path ${CC_SRC_PATH} --lang ${CC_RUNTIME_LANGUAGE} --label ${CHAINCODE_NAME}_${CHAINCODE_VERSION} >&log.txt

## instálo en las organizaciones

Por cada organizacion instalamos el chaincode, cada ejecución obtendremos un id con esta forma **java-bee-manager_1.1:8a4742392b0de102d2b2e1dc6e9a84e6d2767018de16532fbdfe77918951a050** ese id lo deberiamos exportar de la siguiente manera
export CHAINCODE_ID="java-bee-manager_1.1:3b4395875549e7ed8d372877d3f36d050a43c058f7cbb3a0e71d663ea3493673"


	peer lifecycle chaincode install ${CHAINCODE_NAME}.tar.gz


	CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/finanzas.redbee.com/users/Admin@finanzas.redbee.com/msp/ CORE_PEER_ADDRESS=peer0.finanzas.redbee.com:7051 CORE_PEER_LOCALMSPID="FinanzasMSP" CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/finanzas.redbee.com/peers/peer0.finanzas.redbee.com/tls/ca.crt peer lifecycle chaincode install ${CHAINCODE_NAME}.tar.gz


	CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/users/Admin@management.redbee.com/msp/ CORE_PEER_ADDRESS=peer0.management.redbee.com:7051 CORE_PEER_LOCALMSPID="ManagementMSP" CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/peers/peer0.management.redbee.com/tls/ca.crt peer lifecycle chaincode install ${CHAINCODE_NAME}.tar.gz

## Politicas de aprobacion/endorsamiento para el chaincode en el canal

Bien ahora solo resta dar permisos a las organizaciones, no necesariamente todas las organizacion van a tener acceso al chaincode. Podriamos querer que solo CapitalHumanoMSP.peer y FinanzasMSP.peer tengan acceso al mismo

	peer lifecycle chaincode approveformyorg --tls --cafile $ORDERER_CA --channelID $CHANNEL_NAME --name $CHAINCODE_NAME --version $CHAINCODE_VERSION --sequence 1 --waitForEvent --signature-policy "OR ('CapitalHumanoMSP.peer', 'ManagementMSP.peer', 'FinanzasMSP.peer')" --package-id $CHAINCODE_ID

	CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/users/Admin@management.redbee.com/msp/ CORE_PEER_ADDRESS=peer0.management.redbee.com:7051 CORE_PEER_LOCALMSPID="ManagementMSP" CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/peers/peer0.management.redbee.com/tls/ca.crt peer lifecycle chaincode approveformyorg --tls --cafile $ORDERER_CA --channelID $CHANNEL_NAME --name $CHAINCODE_NAME --version $CHAINCODE_VERSION --sequence 1 --waitForEvent --signature-policy "OR ('CapitalHumanoMSP.peer', 'ManagementMSP.peer', 'FinanzasMSP.peer')" --package-id $CHAINCODE_ID


	CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/finanzas.redbee.com/users/Admin@finanzas.redbee.com/msp/ CORE_PEER_ADDRESS=peer0.finanzas.redbee.com:7051 CORE_PEER_LOCALMSPID="FinanzasMSP" CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/finanzas.redbee.com/peers/peer0.finanzas.redbee.com/tls/ca.crt peer lifecycle chaincode approveformyorg --tls --cafile $ORDERER_CA --channelID $CHANNEL_NAME --name $CHAINCODE_NAME --version $CHAINCODE_VERSION --sequence 1 --waitForEvent --signature-policy "OR ('CapitalHumanoMSP.peer', 'ManagementMSP.peer', 'FinanzasMSP.peer')" --package-id $CHAINCODE_ID

## (opcional) validar politicas
Este comando nos  permite ver que organizaciones tienen permisos sobre el chaincode

	peer lifecycle chaincode checkcommitreadiness --channelID $CHANNEL_NAME --name $CHAINCODE_NAME --version $CHAINCODE_VERSION --sequence 1 --signature-policy "OR ('CapitalHumanoMSP.peer', 'ManagementMSP.peer', 'FinanzasMSP.peer')"

## Commitear chaincode

Guardamos la transaccion en la BlockChain

	peer lifecycle chaincode commit -o orderer.redbee.com:7050 --tls --cafile $ORDERER_CA --peerAddresses peer0.capitalHumano.redbee.com:7051 --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/capitalHumano.redbee.com/peers/peer0.capitalHumano.redbee.com/tls/ca.crt --peerAddresses peer0.management.redbee.com:7051 --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/peers/peer0.management.redbee.com/tls/ca.crt --channelID $CHANNEL_NAME --name $CHAINCODE_NAME --version $CHAINCODE_VERSION --sequence 1 --signature-policy "OR ('CapitalHumanoMSP.peer', 'ManagementMSP.peer', 'FinanzasMSP.peer')"


## Probar chaincode
*Work inProgress*

peer chaincode invoke -o orderer.redbee.com:7050 --tls --cafile $ORDERER_CA -C $CHANNEL_NAME -n $CHAINCODE_NAME -c '{"Args": ["createBee", "1", "Ned Flanders", "SSr"]}'

peer chaincode query -o orderer.redbee.com:7050 --tls --cafile $ORDERER_CA -C $CHANNEL_NAME -n $CHAINCODE_NAME -c '{"Args": ["getBee","1"]}'

peer chaincode invoke -o orderer.redbee.com:7050 --tls --cafile $ORDERER_CA -C $CHANNEL_NAME -n $CHAINCODE_NAME -c '{"Args": ["managementUpdate", "1", "Sr 1", "Reverendo Alegria", "Proyecto X"]}'

peer chaincode query -o orderer.redbee.com:7050 --tls --cafile $ORDERER_CA -C $CHANNEL_NAME -n $CHAINCODE_NAME -c '{"Args": ["getBeeHistory","1"]}'


# Material

Documentación: https://hyperledger-fabric.readthedocs.io/en/release-2.5/

Canal youtube HyperLedge Fundation: https://www.youtube.com/c/Hyperledger

Discord comunidad HyperLedger: https://discord.com/invite/hyperledger

Curso Hyperledger ordenado: https://wiki.hyperledger.org/display/CP/Curso++Hyperledger+Fabric
