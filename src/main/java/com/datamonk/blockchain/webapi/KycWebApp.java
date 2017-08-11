package com.datamonk.blockchain.webapi;

import com.datamonk.blockchain.hyperledger.ChainService;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by namakilam on 04/08/17.
 */
@ApplicationPath("rest")
public class KycWebApp extends Application {
    private Set<Object> singletons = new HashSet<Object>();

    public KycWebApp() throws Exception {
        ChainService chainService = new ChainService();
        singletons.add(chainService);
        singletons.add(new API(chainService));
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
