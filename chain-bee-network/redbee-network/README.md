## copiar a vm
scp -i ~/.ssh/chainbee.pem curso.tar.gz ubuntu@54.87.108.161:hyperledge/

### Instalar hyperledger

FABRICSamplesDir="$HOME/hyperledger/fabric"
mkdir -p $FABRICSamplesDir

sudo chmod -R 777 $FABRICSamplesDir
cd $FABRICSamplesDir
sudo curl -sSL http://bit.ly/2ysbOFE | bash -s 2.4

echo 'export PATH=$PATH:$HOME/hyperledger/fabric/fabric-samples/bin' >> ~/.profile
source ~/.profile
source $HOME/.profile

## portainer
docker volume create portainer_data

docker run -d -p 8000:8000 -p 9443:9443 --name portainer --restart=always -v /var/run/docker.sock:/var/run/docker.sock -v portainer_data:/data portainer/portainer-ce:latest


entrar a ~/hyperledger/fabric/curso-hyperledger-fabric/redbee-network
## MAterial criptografico
export CHANNEL_NAME=chainbee
export VERBOSE=false
export FABRIC_CFG_PATH=$PWD

cryptogen generate --config=./crypto-config.yaml

## bloque genesis

configtxgen  -profile ThreeOrgsOrdererGenesis -channelID system-channel -outputBlock ./channel-artifacts/genesis.block

## tx de canal
configtxgen -profile ThreeOrgsChannel -outputCreateChannelTx ./channel-artifacts/channel.tx -channelID $CHANNEL_NAME

## crea anchors peers
configtxgen -profile ThreeOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/CapitalHumanoMSPanchors.tx -channelID $CHANNEL_NAME -asOrg CapitalHumanoMSP

configtxgen -profile ThreeOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/FinanzasMSPanchors.tx -channelID $CHANNEL_NAME -asOrg FinanzasMSP

configtxgen -profile ThreeOrgsChannel -outputAnchorPeersUpdate ./channel-artifacts/ManagementMSPanchors.tx -channelID $CHANNEL_NAME -asOrg ManagementMSP

## docker levanta peers, orderer, ca, cli, couch
CHANNEL_NAME=$CHANNEL_NAME docker-compose -f docker-compose-cli-couchdb.yaml up -d

##Crear Canal
Entrar a la consola de la cli

export CHANNEL_NAME=chainbee

peer channel create -o orderer.redbee.com:7050 -c $CHANNEL_NAME -f ./channel-artifacts/channel.tx --tls false --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/redbee.com/msp/tlscacerts/tlsca.redbee.com-cert.pem


## unirse al canal

peer channel join -b chainbee.block

CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/users/Admin@management.redbee.com/msp/ CORE_PEER_ADDRESS=peer0.management.redbee.com:7051 CORE_PEER_LOCALMSPID="ManagementMSP" CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/peers/peer0.management.redbee.com/tls/ca.crt peer channel join -b chainbee.block

CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/finanzas.redbee.com/users/Admin@finanzas.redbee.com/msp/ CORE_PEER_ADDRESS=peer0.finanzas.redbee.com:7051 CORE_PEER_LOCALMSPID="FinanzasMSP" CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/finanzas.redbee.com/peers/peer0.finanzas.redbee.com/tls/ca.crt peer channel join -b chainbee.block


## AnchorsPeers

peer channel update -o orderer.redbee.com:7050 -c $CHANNEL_NAME -f ./channel-artifacts/CapitalHumanoMSPanchors.tx --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/redbee.com/msp/tlscacerts/tlsca.redbee.com-cert.pem

CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/users/Admin@management.redbee.com/msp/ CORE_PEER_ADDRESS=peer0.management.redbee.com:7051 CORE_PEER_LOCALMSPID="ManagementMSP" CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/peers/peer0.management.redbee.com/tls/ca.crt  peer channel update -o orderer.redbee.com:7050 -c $CHANNEL_NAME -f ./channel-artifacts/ManagementMSPanchors.tx --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/redbee.com/msp/tlscacerts/tlsca.redbee.com-cert.pem

CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/finanzas.redbee.com/users/Admin@finanzas.redbee.com/msp/ CORE_PEER_ADDRESS=peer0.finanzas.redbee.com:7051 CORE_PEER_LOCALMSPID="FinanzasMSP" CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/finanzas.redbee.com/peers/peer0.finanzas.redbee.com/tls/ca.crt peer channel update -o orderer.redbee.com:7050 -c $CHANNEL_NAME -f ./channel-artifacts/FinanzasMSPanchors.tx --tls true --cafile /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/redbee.com/msp/tlscacerts/tlsca.redbee.com-cert.pem



## Chaincode Java
### Build
./gradlew installDist

export CHANNEL_NAME=chainbee && export CHAINCODE_NAME=java-bee-manager && export CHAINCODE_VERSION=1.1 && export CC_RUNTIME_LANGUAGE=java && export CC_SRC_PATH="../../../chaincode/BeeManager/build/install/java-bee-manager" && export ORDERER_CA=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/redbee.com/msp/tlscacerts/tlsca.redbee.com-cert.pem


export CHAINCODE_ID="java-bee-manager_1.1:8a4742392b0de102d2b2e1dc6e9a84e6d2767018de16532fbdfe77918951a050"

## Chaincode Go

export CHANNEL_NAME=chainbee
export CHAINCODE_NAME=foodcontrol
export CHAINCODE_VERSION=1
export CC_RUNTIME_LANGUAGE=golang
export CC_SRC_PATH="../../../chaincode/$CHAINCODE_NAME/"
export ORDERER_CA=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/redbee.com/msp/tlscacerts/tlsca.redbee.com-cert.pem


## empaqueta el chaincode
peer lifecycle chaincode package ${CHAINCODE_NAME}.tar.gz --path ${CC_SRC_PATH} --lang ${CC_RUNTIME_LANGUAGE} --label ${CHAINCODE_NAME}_${CHAINCODE_VERSION} >&log.txt

## instalo en organizacion (copiar id resultante)
peer lifecycle chaincode install ${CHAINCODE_NAME}.tar.gz


CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/finanzas.redbee.com/users/Admin@finanzas.redbee.com/msp/ CORE_PEER_ADDRESS=peer0.finanzas.redbee.com:7051 CORE_PEER_LOCALMSPID="FinanzasMSP" CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/finanzas.redbee.com/peers/peer0.finanzas.redbee.com/tls/ca.crt peer lifecycle chaincode install ${CHAINCODE_NAME}.tar.gz


CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/users/Admin@management.redbee.com/msp/ CORE_PEER_ADDRESS=peer0.management.redbee.com:7051 CORE_PEER_LOCALMSPID="ManagementMSP" CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/peers/peer0.management.redbee.com/tls/ca.crt peer lifecycle chaincode install ${CHAINCODE_NAME}.tar.gz

## Politicas de aprobacion/endorsamiento para el chaincode en el canal (aca capital y management pueden escribir transacciones nuevas)

peer lifecycle chaincode approveformyorg --tls --cafile $ORDERER_CA --channelID $CHANNEL_NAME --name $CHAINCODE_NAME --version $CHAINCODE_VERSION --sequence 1 --waitForEvent --signature-policy "OR ('CapitalHumanoMSP.peer', 'ManagementMSP.peer', 'FinanzasMSP.peer')" --package-id $CHAINCODE_ID

CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/users/Admin@management.redbee.com/msp/ CORE_PEER_ADDRESS=peer0.management.redbee.com:7051 CORE_PEER_LOCALMSPID="ManagementMSP" CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/peers/peer0.management.redbee.com/tls/ca.crt peer lifecycle chaincode approveformyorg --tls --cafile $ORDERER_CA --channelID $CHANNEL_NAME --name $CHAINCODE_NAME --version $CHAINCODE_VERSION --sequence 1 --waitForEvent --signature-policy "OR ('CapitalHumanoMSP.peer', 'ManagementMSP.peer', 'FinanzasMSP.peer')" --package-id $CHAINCODE_ID


CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/finanzas.redbee.com/users/Admin@finanzas.redbee.com/msp/ CORE_PEER_ADDRESS=peer0.finanzas.redbee.com:7051 CORE_PEER_LOCALMSPID="FinanzasMSP" CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/finanzas.redbee.com/peers/peer0.finanzas.redbee.com/tls/ca.crt peer lifecycle chaincode approveformyorg --tls --cafile $ORDERER_CA --channelID $CHANNEL_NAME --name $CHAINCODE_NAME --version $CHAINCODE_VERSION --sequence 1 --waitForEvent --signature-policy "OR ('CapitalHumanoMSP.peer', 'ManagementMSP.peer', 'FinanzasMSP.peer')" --package-id $CHAINCODE_ID

validar politicas

peer lifecycle chaincode checkcommitreadiness --channelID $CHANNEL_NAME --name $CHAINCODE_NAME --version $CHAINCODE_VERSION --sequence 1 --signature-policy "OR ('CapitalHumanoMSP.peer', 'ManagementMSP.peer', 'FinanzasMSP.peer')"

##commitear chaincode

peer lifecycle chaincode commit -o orderer.redbee.com:7050 --tls --cafile $ORDERER_CA --peerAddresses peer0.capitalHumano.redbee.com:7051 --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/capitalHumano.redbee.com/peers/peer0.capitalHumano.redbee.com/tls/ca.crt --peerAddresses peer0.management.redbee.com:7051 --tlsRootCertFiles /opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/management.redbee.com/peers/peer0.management.redbee.com/tls/ca.crt --channelID $CHANNEL_NAME --name $CHAINCODE_NAME --version $CHAINCODE_VERSION --sequence 1 --signature-policy "OR ('CapitalHumanoMSP.peer', 'ManagementMSP.peer', 'FinanzasMSP.peer')"


##probar chaincode

peer chaincode invoke -o orderer.redbee.com:7050 --tls --cafile $ORDERER_CA -C $CHANNEL_NAME -n $CHAINCODE_NAME -c '{"Args": ["Set", "did:7", "ricardo", "banana"]}'


peer chaincode query -o orderer.redbee.com:7050 --tls --cafile $ORDERER_CA -C $CHANNEL_NAME -n $CHAINCODE_NAME -c '{"Args": ["Query","did:7"]}'