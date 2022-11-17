package com.redbee.chainbeeapp.config;


import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.identity.Identities;
import org.hyperledger.fabric.client.identity.Identity;
import org.hyperledger.fabric.client.identity.Signer;
import org.hyperledger.fabric.client.identity.Signers;
import org.hyperledger.fabric.client.identity.X509Identity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

@Configuration
public class GatewayConfig {
    private static final String mspID = "CapitalHumanoMSP";
    private static final String channelName = "chainbee";
    private static final String chaincodeName = "java-bee-manager";

    // Path to crypto materials.
    private static final Path cryptoPath = Paths.get("..", "redbee-network", "crypto-config", "peerOrganizations", "capitalHumano.redbee.com");
    // Path to user certificate.
    private static final Path certPath = cryptoPath.resolve(Paths.get("users", "User1@capitalHumano.redbee.com", "msp", "signcerts", "User1@capitalHumano.redbee.com-cert.pem"));
    // Path to user private key directory.
    private static final Path keyDirPath = cryptoPath.resolve(Paths.get("users", "User1@capitalHumano.redbee.com", "msp", "keystore"));
    // Path to peer tls certificate.
    private static final Path tlsCertPath = cryptoPath.resolve(Paths.get("peers", "peer0.capitalHumano.redbee.com", "tls", "ca.crt"));

    // Gateway peer end point.
    private static final String peerEndpoint = "localhost:7051";
    private static final String overrideAuth = "peer0.capitalHumano.redbee.com";

    private final Contract contract;

    public GatewayConfig() throws Exception {
        Gateway gateway = buildGateway();
        // Get a network instance representing the channel where the smart contract is
        // deployed.
        var network = gateway.getNetwork(channelName);
        // Get the smart contract from the network.
        this.contract = network.getContract(chaincodeName);
    }

    @Bean(name = "contract")
    public Contract getContract() {
        return this.contract;
    }

    public Gateway buildGateway() throws Exception {
        // The gRPC client connection should be shared by all Gateway connections to
        // this endpoint.
        var channel = newGrpcConnection();

        var builder = Gateway.newInstance().identity(newIdentity()).signer(newSigner()).connection(channel)
            // Default timeouts for different gRPC calls
            .evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
            .endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
            .submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
            .commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));

        try (var gateway = builder.connect()) {
            return gateway;
        }
    }

    private static ManagedChannel newGrpcConnection() throws IOException, CertificateException {
        var tlsCertReader = Files.newBufferedReader(tlsCertPath);
        var tlsCert = Identities.readX509Certificate(tlsCertReader);

        return NettyChannelBuilder.forTarget(peerEndpoint)
            .sslContext(GrpcSslContexts.forClient().trustManager(tlsCert).build()).overrideAuthority(overrideAuth)
            .build();
    }

    private static Identity newIdentity() throws IOException, CertificateException {
        var certReader = Files.newBufferedReader(certPath);
        var certificate = Identities.readX509Certificate(certReader);

        return new X509Identity(mspID, certificate);
    }

    private static Signer newSigner() throws IOException, InvalidKeyException {
        var keyReader = Files.newBufferedReader(getPrivateKeyPath());
        var privateKey = Identities.readPrivateKey(keyReader);

        return Signers.newPrivateKeySigner(privateKey);
    }

    private static Path getPrivateKeyPath() throws IOException {
        try (var keyFiles = Files.list(keyDirPath)) {
            return keyFiles.findFirst().orElseThrow();
        }
    }
}