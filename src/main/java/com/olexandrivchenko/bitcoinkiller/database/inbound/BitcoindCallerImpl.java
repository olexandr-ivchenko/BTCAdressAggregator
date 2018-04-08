package com.olexandrivchenko.bitcoinkiller.database.inbound;

import com.olexandrivchenko.bitcoinkiller.database.inbound.BitcoindCaller;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Block;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.GenericResponse;
import com.olexandrivchenko.bitcoinkiller.database.inbound.jsonrpc.Tx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class BitcoindCallerImpl implements BitcoindCaller {
    private final static Logger log = LoggerFactory.getLogger(BitcoindCallerImpl.class);

    @Autowired
    private BitcoindServiceImpl service;

    @Override
    public long getBlockchainSize() {
        log.debug("getBlockchainSize");
        GenericResponse<Long> rs = service.call("getblockcount", null, new ParameterizedTypeReference<GenericResponse<Long>> (){});
        return rs.getResult();
    }

    @Override
    public GenericResponse<Block> getBlock(long number) {
        log.debug("Loading block id={}", number);

        List<Object> params = new ArrayList<>();
        params.add(number);
        GenericResponse<String> blockhash = service.call("getblockhash", params, new ParameterizedTypeReference<GenericResponse<String>> (){});

        params.clear();
        params.add(blockhash.getResult());
        params.add(2); //verbosity level. 2 means return encoded transactions
        GenericResponse<Block> blockBody = service.call("getblock", params, new ParameterizedTypeReference<GenericResponse<Block>> (){});

        return blockBody;
    }

    @Override
    public Tx loadTransaction(String txid) {
        log.debug("Loading transaction {}", txid);
        List<Object> params = new ArrayList<>();
        params.add(txid);
        params.add(true); //verbose. true means return json, instead of hex

        GenericResponse<Tx> tx = service.call("getrawtransaction", params, new ParameterizedTypeReference<GenericResponse<Tx>> (){});
        return tx.getResult();
    }
}
