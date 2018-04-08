package com.olexandrivchenko.bitcoinkiller.database.main;

import com.olexandrivchenko.bitcoinkiller.database.inbound.BitcoindCaller;
import com.olexandrivchenko.bitcoinkiller.database.inbound.BitcoindCallerImpl;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Tx;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Vin;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Vout;
import com.olexandrivchenko.bitcoinkiller.database.outbound.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TxToAddressConverter {

    @Autowired
    private BitcoindCaller daemon;

    public List<Address> convert(Tx tx) {
        List<Vin> vin = tx.getVin();
        List<Vout> vout = tx.getVout();

        List<Address> rs = new ArrayList<>();
        if (!isBlockReward(vin)) {
            for (Vin vinn : vin) {
                Tx inputTransaction = daemon.loadTransaction(vinn.getTxid());
                Vout outForThisInput = inputTransaction.getVout().get(vinn.getVout());
                Address inputAddr = getAddress(outForThisInput, false);
                rs.add(inputAddr);
            }
        }
        for (Vout voutt : vout) {
            Address outAddr = getAddress(voutt, true);
            rs.add(outAddr);
        }
        return rs;
    }

    private boolean isBlockReward(List<Vin> vin) {
        if (vin.size() == 1
                && vin.get(0).getCoinbase() != null
                && vin.get(0).getTxid() == null
                && vin.get(0).getScriptSig() == null) {
            //this is mining block reward - no input, just coinbase
            return true;
        }
        return false;
    }

    private Address getAddress(Vout out, boolean isAdded) {
        Address addr = new Address();
        addr.setAmount(isAdded ? out.getValue() : -out.getValue());
        addr.setAddress(out.getScriptPubKey().getAddresses().get(0));
        if (out.getScriptPubKey().getAddresses().size() > 1) {
            throw new Error("review this!!!");
        }
        return addr;
    }
}
