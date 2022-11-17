package com.redbee.chainbeeapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.redbee.chainbeeapp.model.Bee;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.client.CommitException;
import org.hyperledger.fabric.client.CommitStatusException;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.EndorseException;
import org.hyperledger.fabric.client.GatewayException;
import org.hyperledger.fabric.client.SubmitException;
import org.hyperledger.fabric.client.SubmittedTransaction;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class ChainbeeService {

    private static final String TX_CREATE_BEE = "createBee";
    private static final String TX_GET_HISTORY = "getBeeHistory";
    private static final String TX_GET_BEE = "getBee";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final String TX_MANAGEMENT = "managementUpdate";
    public static final String TX_FINANCIAL = "financeUpdate";
    private final Contract contract;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChainbeeService(Contract contract) {
        this.contract = contract;
    }

    private final Map<String, Function<Bee, SubmittedTransaction>> factory = Map.of(
        TX_MANAGEMENT, this::getSubmittedTransactionManagement,
        TX_FINANCIAL, this::getSubmittedTransactionFinancial
    );

    /**
     * This type of transaction would typically only be run once by an application
     * the first time it was started after its initial deployment. A new version of
     * the chaincode deployed later would likely not need to run an "init" function.
     */
    private void initLedger() throws EndorseException, SubmitException, CommitStatusException, CommitException {
        log.info("Submit Transaction: InitLedger, function creates the initial set of assets on the ledger");

        contract.submitTransaction("initLedger");

        log.info("*** Transaction committed successfully");
    }

    /**
     * Submit a transaction synchronously, blocking until it has been committed to
     * the ledger.
     */
    public void createAsset(Bee bee) throws EndorseException, SubmitException, CommitStatusException, CommitException {
        log.info("Submit Transaction: {}, creates new asset with ID, Name and Seniority arguments", TX_CREATE_BEE);

        contract.submitTransaction(TX_CREATE_BEE, bee.getId(), bee.getName(), bee.getSeniority());

        log.info("*** Transaction committed successfully ***");
    }

    /**
     * Submit transaction asynchronously, allowing the application to process the
     * smart contract response (e.g. update a UI) while waiting for the commit
     * notification.
     */
    public void asyncUpdate(Bee bee, String transactionName) throws EndorseException, SubmitException, CommitStatusException {
        log.info("Async Submit Transaction: {}, updates existing asset", transactionName);

        SubmittedTransaction commit = factory.get(transactionName).apply(bee);

        var result = commit.getResult();
        log.info("Result: {}", new String(result, StandardCharsets.UTF_8));
        log.info("*** Successfully submitted transaction ***");
        validateCommitStatus(commit);
    }

    private static void validateCommitStatus(SubmittedTransaction commit) throws CommitStatusException {
        log.info("*** Waiting for transaction commit ***");
        var status = commit.getStatus();
        if (!status.isSuccessful()) {
            log.error("Transaction: {} failed with status: {}", status.getTransactionId(), status.getCode());
            throw new RuntimeException("Transaction " + status.getTransactionId() +
                " failed to commit with status code " + status.getCode());
        }
        log.info("*** Transaction committed successfully ***");
    }

    private SubmittedTransaction getSubmittedTransactionManagement(Bee bee) {
        try {
            return contract.newProposal(TX_MANAGEMENT)
                .addArguments(bee.getSeniority(), bee.getManager(), bee.getAssignment(), bee.getId())
                .build()
                .endorse()
                .submitAsync();
        } catch (SubmitException | EndorseException e) {
            log.error("Error executing transaction: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private SubmittedTransaction getSubmittedTransactionFinancial(Bee bee) {
        try {
            return contract.newProposal(TX_FINANCIAL)
                .addArguments(bee.getSalary().toString(), String.valueOf(bee.isHasMixedSalary()), bee.getId())
                .build()
                .endorse()
                .submitAsync();
        } catch (SubmitException | EndorseException e) {
            log.error("Error executing transaction: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Bee> getHistory(String id) throws GatewayException, JsonProcessingException {
        log.info("Evaluate Transaction: {}, function returns all the current assets on the ledger", TX_GET_HISTORY);
        var result = contract.evaluateTransaction(TX_GET_HISTORY, id);
        return objectMapper.readValue(prettyJson(result), new TypeReference<List<Bee>>() {
        });
    }

    public Bee getBee(String id) throws GatewayException, JsonProcessingException {
        log.info("Evaluate Transaction: {}, function returns asset attributes", TX_GET_BEE);
        var evaluateResult = contract.evaluateTransaction(TX_GET_BEE, id);
        return objectMapper.readValue(prettyJson(evaluateResult), Bee.class);
    }

    private String prettyJson(final byte[] json) {
        return prettyJson(new String(json, StandardCharsets.UTF_8));
    }

    private String prettyJson(final String json) {
        var parsedJson = JsonParser.parseString(json);
        return gson.toJson(parsedJson);
    }
}