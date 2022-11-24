package com.redbee.chainbee.beemanager;

import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.ArrayList;

@Contract(name = "BeeManager", info = @Info(title = "BeeManager contract", description = "BeeManager sample contract",
    version = "0.0.1-SNAPSHOT"))
@Default
public final class BeeManager implements ContractInterface {

    private static final String HR_MSP_ID = "CapitalHumanoMSP";
    private static final String MANAGEMENT_MSP_ID = "ManagementMSP";
    private static final String FINANCE_MSP_ID = "FinanzasMSP";
    private static final Genson genson = new Genson();

    private enum BeeManagerErrors {
        BEE_NOT_FOUND, BEE_ALREADY_EXISTS, SALARY_FORMAT_ERROR, INVALID_MSP
    }

    @Transaction()
    public void initLedger(final Context ctx) {
    }

    @Transaction()
    public Bee createBee(final Context ctx, final Integer beeId, final String beeName, final String beeSeniority) {

        if (beeId == null || beeName.isEmpty() || beeSeniority.isEmpty())
            throw new ChaincodeException("Los campos {id} - {name} - {seniority} son requeridos");

        ChaincodeStub stub = ctx.getStub();

//        validateMSP(stub, HR_MSP_ID);

        String beeState = stub.getStringState(beeId.toString());
        if (!beeState.isEmpty())
            throw new ChaincodeException(String.format("La abeja %s ya existe", beeId.toString()), BeeManagerErrors.BEE_ALREADY_EXISTS.toString());

        Bee bee = new Bee(true, beeName, beeId, beeSeniority, null, null, false, null);
        beeState = genson.serialize(bee);
        stub.putStringState(beeId.toString(), beeState);
        return bee;
    }

    @Transaction()
    public Bee getBee(final Context ctx, final Integer beeId) {
        ChaincodeStub stub = ctx.getStub();
        return getBeeState(beeId, stub);
    }

    @Transaction()
    public String managementUpdate(final Context ctx, final String newSeniority, final String newManager,
                                   final String newAssignment, final Integer beeId) {

        ChaincodeStub stub = ctx.getStub();

//        validateMSP(stub, MANAGEMENT_MSP_ID);

        Bee actualBee = getBeeState(beeId, stub);
        Bee newBee = new Bee(actualBee.getIsActive(), actualBee.getName(), actualBee.getId(), newSeniority, newManager,
            newAssignment, actualBee.getHasMixedSalary(), actualBee.getSalary());

        String newBeeState = genson.serialize(newBee);
        stub.putStringState(beeId.toString(), newBeeState);

        return "Bee: " + beeId.toString() + " Updated";
    }

    @Transaction()
    public String financeUpdate(final Context ctx, final String salary, final Boolean hasMixedSalary,
                                final Integer beeId) {
        double newSalary = 0.0;
        try {
            newSalary = Double.parseDouble(salary);
            if (newSalary < 0.0) {
                throw new ChaincodeException(String.format("Salary %s error", salary), BeeManagerErrors.SALARY_FORMAT_ERROR.toString());
            }

        } catch (NumberFormatException e) {
            throw new ChaincodeException(e);
        }

        ChaincodeStub stub = ctx.getStub();

//        validateMSP(stub, FINANCE_MSP_ID);

        Bee actualBee = getBeeState(beeId, stub);
        Bee newBee = new Bee(actualBee.getIsActive(), actualBee.getName(), actualBee.getId(), actualBee.getSeniority(), actualBee.getManager(),
            actualBee.getAssignment(), hasMixedSalary, newSalary);

        String newBeeState = genson.serialize(newBee);
        stub.putStringState(beeId.toString(), newBeeState);

        return "Bee: " + beeId.toString() + " Updated";
    }

    @Transaction
    public String getBeeHistory(final Context ctx, final String beeId) {
        if (beeId == null) {
            throw new ChaincodeException("El campo beeId es requerido");
        }
        ChaincodeStub stub = ctx.getStub();
        ArrayList<String> results = new ArrayList<>();
        try {
            QueryResultsIterator<KeyModification> history = stub.getHistoryForKey(beeId);
            validateHistory(beeId, history);
            addHistory(results, history);
            history.close();
        } catch (Exception e) {
            throw new ChaincodeException(e);
        }
        return results.toString();
    }

    @Transaction()
    public String deleteBee(Context ctx, final Integer beeId) {
        ChaincodeStub stub = ctx.getStub();
        getBeeState(beeId, stub);
        ctx.getStub().delState(beeId.toString());
        return "Bee: " + beeId.toString() + " Deleted";
    }

    private static void addHistory(ArrayList<String> results, QueryResultsIterator<KeyModification> history) {
        for (KeyModification keyModification : history) {
            String value = keyModification.getStringValue();
            results.add(value);
        }
    }

    private static void validateHistory(String beeId, QueryResultsIterator<KeyModification> history) {
        if (history == null) {
            String errorMessage = String.format("Product %s does not exist", beeId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, BeeManagerErrors.BEE_NOT_FOUND.toString());
        }
    }

    private static Bee getBeeState(Integer beeId, ChaincodeStub stub) {
        String beeState = stub.getStringState(beeId.toString());

        if (beeState.isEmpty())
            throw new ChaincodeException(String.format("La abeja %s no existe", beeId), BeeManagerErrors.BEE_NOT_FOUND.toString());

        return genson.deserialize(beeState, Bee.class);
    }

    private static void validateMSP(ChaincodeStub stub, String authorizedMSP) {
        String mspId = stub.getMspId();
        if (!authorizedMSP.equals(mspId))
            throw new ChaincodeException(String.format("%s no tiene permisos para ejecutar esta accion", mspId), BeeManagerErrors.INVALID_MSP.toString());
    }
}
