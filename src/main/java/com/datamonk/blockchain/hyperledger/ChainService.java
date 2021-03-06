package com.datamonk.blockchain.hyperledger;

import com.datamonk.blockchain.config.*;
import com.datamonk.blockchain.config.helper.NetworkConfigHelper;
import com.datamonk.blockchain.hyperledger.exceptions.*;
import com.datamonk.blockchain.webapi.API;
import com.datamonk.blockchain.webapi.pojo.Asset;
import com.datamonk.blockchain.webapi.pojo.ChainCodeIDPojo;
import com.datamonk.blockchain.webapi.pojo.KYCUser;
import com.datamonk.blockchain.webapi.requests.APIRequest;
import com.datamonk.blockchain.webapi.requests.RequestParams;
import com.google.protobuf.ByteString;
import org.apache.commons.codec.binary.Hex;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.QueryException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric_ca.sdk.exception.RegistrationException;

import javax.ws.rs.BadRequestException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.datamonk.blockchain.config.Util.out;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by namakilam on 04/08/17.
 */
public class ChainService {
    private static final NetworkConfig config = NetworkConfig.getConfig();
    private static final String TEST_ADMIN_NAME = "admin";
    private static final String TESTUSER_1_NAME = "user1";
    private static final String TEST_FIXTURES_PATH = "/Users/namakilam/workspace/go/src/github.com/hyperledger/fabric/examples/e2e_cli";
    private static final String CHAIN_CODE_NAME = "kyc_cc";
    private static final String CHAIN_CODE_PATH = "github.com/example_cc";
    private static final String CHAIN_CODE_VERSION = "2.3.6";
    private static final String PROPERTY_CHAIN_CODE_NAME = "property_cc";
    private static final String PROPERTY_CHAIN_CODE_PATH = "github.com/property_chaincode";
    private static final String PROPERTY_CHAIN_CODE_VERSION = "2.4.7";

    private static final String CHANNEL_NAME = "mychannel";

    private static final NetworkConfigHelper configHelper = new NetworkConfigHelper();

    private static final String ACCEPT_TRANSFER_PROPERTY_METHOD_KEY = "acceptTransferRequest";
    private static final String TRANSFER_PROPERTY_METHOD_KEY = "transferRequest";
    private static final String INSERT_ASSET_METHOD_KEY = "insert";
    private static final String GET_PROPERTY_ID_METHOD_KEY = "getById";
    private static final String GET_PROPERTY_OWNER_METHOD_KEY = "getByOwner";
    private static final String UPDATE_METHOD_KEY = "update";
    private static final String HISTORY_METHOD_KEY = "history";
    private static final String RETRIEVE_METHOD_KEY = "retrieve";
    private static final String INSERT_METHOD_KEY = "insert";
    private static final String RESULT_KEY =  "result";
    private static final String APPROVE_TRANSFER_PROPERTY_METHOD_KEY = "approveTransferRequest";
    private static final String TRANSACTION_ID_KEY = "transaction_id";
    private static final String BLOCK_NUMBER_KEY = "block";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private HFClient client;
    private Channel kycChannel;
    private SampleStore sampleStore;
    private SampleOrg sampleOrg;
    private Collection<Orderer> orderers;
    private ChaincodeID kycChaincodeId;
    private ChaincodeID propertyChaincodeId;
    Collection<ProposalResponse> responses;
    Collection<ProposalResponse> successful = new LinkedList<>();
    Collection<ProposalResponse> failed = new LinkedList<>();

    private static Collection<SampleOrg> testSampleOrgs;

    public ChainService() throws Exception {
        init();
    }

    private void setupConfig() throws NoSuchFieldException, IllegalAccessException, MalformedURLException {
        configHelper.clearConfig();
        configHelper.customizeConfig();

        testSampleOrgs = config.getIntegrationTestsSampleOrgs();

        for (SampleOrg sampleOrg : testSampleOrgs) {
            sampleOrg.setCAClient(HFCAClient.createNewInstance(sampleOrg.getCALocation(), sampleOrg.getCAProperties()));
        }
    }

    private void init() throws Exception {
        setupConfig();

        client = HFClient.createNewInstance();
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());


        File sampleStoreFile = new File(System.getProperty("java.io.tmpdir") + "/HFCSampletest.properties");
        /*if (sampleStoreFile.exists()) { //For testing start fresh
            sampleStoreFile.delete();
        }*/

        sampleStore = new SampleStore(sampleStoreFile);

        for (SampleOrg sampleOrg : testSampleOrgs) {

            registerAndEnrollAdmin(sampleOrg);

            registerAndEnrollUser(sampleOrg);

            setPeerOrgAdmin(sampleOrg);
        }

        sampleOrg = config.getIntegrationTestsSampleOrg("peerOrg1");
        client.setUserContext(sampleOrg.getPeerAdmin());

        populateOrderers();

        for (String peerName : sampleOrg.getPeerNames()) {
            String peerLocation = sampleOrg.getPeerLocation(peerName);

            Properties peerProperties = config.getPeerProperties(peerName); //test properties for peer.. if any.
            if (peerProperties == null) {
                peerProperties = new Properties();
            }
            peerProperties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);

            Peer peer = client.newPeer(peerName, peerLocation, peerProperties);
            sampleOrg.addPeer(peer);
        }


        kycChannel = constructChannel(CHANNEL_NAME);

        Boolean install = true;
        ChaincodeID chaincodeId = ChaincodeID.newBuilder().setName(CHAIN_CODE_NAME)
                .setPath(CHAIN_CODE_PATH)
                .setVersion(CHAIN_CODE_VERSION)
                .build();

        List<Query.ChaincodeInfo> instantiatedChaincodes = kycChannel.queryInstantiatedChaincodes(sampleOrg.getPeers().iterator().next());

        for (Query.ChaincodeInfo chainCodeInfo : instantiatedChaincodes) {
            if (Objects.equals(chainCodeInfo.getName(), chaincodeId.getName()) && Objects.equals(chainCodeInfo.getPath(), chaincodeId.getPath()) && Objects.equals(chainCodeInfo.getVersion(), chaincodeId.getVersion())) {
                install = false;
            }
        }

        if (install) {
            kycChaincodeId = installChaincode(chaincodeId);
            instantiateChaincode(kycChaincodeId);
        } else {
            kycChaincodeId = chaincodeId;
        }

        install = true;
        ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(PROPERTY_CHAIN_CODE_NAME)
                .setPath(PROPERTY_CHAIN_CODE_PATH)
                .setVersion(PROPERTY_CHAIN_CODE_VERSION)
                .build();

        for (Query.ChaincodeInfo chainCodeInfo : instantiatedChaincodes) {
            if (Objects.equals(chainCodeInfo.getName(), chaincodeID.getName()) && Objects.equals(chainCodeInfo.getPath(), chaincodeID.getPath()) && Objects.equals(chainCodeInfo.getVersion(), chaincodeID.getVersion())) {
                install = false;
            }
        }

        if (install) {
            propertyChaincodeId = installChaincode(chaincodeID);
            instantiateChaincode(propertyChaincodeId);
        } else {
            propertyChaincodeId = chaincodeID;
        }
    }

    private void instantiateChaincode(ChaincodeID chaincodeID) throws org.hyperledger.fabric.sdk.exception.InvalidArgumentException, IOException, ChaincodeEndorsementPolicyParseException, ProposalException, NotEnoughEndorsersException, ChaincodeInstantiationFailedException {
        InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
        instantiateProposalRequest.setProposalWaitTime(config.getProposalWaitTime());
        instantiateProposalRequest.setChaincodeID(chaincodeID);
        instantiateProposalRequest.setFcn("init");
        instantiateProposalRequest.setArgs(new String[]{"init"});

        Map<String, byte[]> tm = new HashMap<>();
        tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
        tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
        instantiateProposalRequest.setTransientMap(tm);

        ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
        chaincodeEndorsementPolicy.fromYamlFile(new File(TEST_FIXTURES_PATH + "/chaincodeendorsementpolicy.yaml"));
        instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

        out("Sending instantiateProposalRequest to all peers");
        successful.clear();
        failed.clear();

        responses = kycChannel.sendInstantiationProposal(instantiateProposalRequest);
        for (ProposalResponse response : responses) {
            if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
                successful.add(response);
                out("Succesful instantiate proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
            } else {
                failed.add(response);
            }
        }
        out("Received %d instantiate proposal responses. Successful+verified: %d . Failed: %d", responses.size(), successful.size(), failed.size());
        if (failed.size() > 0) {
            ProposalResponse first = failed.iterator().next();
            out("Not enough endorsers for instantiate :" + successful.size() + "endorser failed with " + first.getMessage() + ". Was verified:" + first.isVerified());
            throw new NotEnoughEndorsersException();
        }


        out("Sending instantiateTransaction to orderer");

        try {
            kycChannel.sendTransaction(successful, orderers).get(config.getTransactionWaitTime(), TimeUnit.SECONDS);

        } catch (Exception e) {
            throw new ChaincodeInstantiationFailedException(e);
        }
    }

    private ChaincodeID installChaincode(ChaincodeID chaincodeId) throws org.hyperledger.fabric.sdk.exception.InvalidArgumentException, IOException, ProposalException, NotEnoughEndorsersException {

        if (client.getUserContext() != sampleOrg.getPeerAdmin()) {
            client.setUserContext(sampleOrg.getPeerAdmin());
        }

        out("Creating install proposal");

        InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
        installProposalRequest.setChaincodeID(chaincodeId);

        installProposalRequest.setChaincodeInputStream(Util.generateTarGzInputStream(
                Paths.get("/Users/namakilam/workspace/go/kyc_web_app/src/main/chaincode", "/sdkintegration/gocc/sample1", "src", chaincodeId.getPath()).toFile(),
                Paths.get("src", chaincodeId.getPath()).toString()
        ));


        installProposalRequest.setChaincodeVersion(chaincodeId.getVersion());

        int numInstallProposal = 0;
        //    Set<String> orgs = orgPeers.keySet();
        //   for (SampleOrg org : testSampleOrgs) {

        Set<Peer> peersFromOrg = sampleOrg.getPeers();
        numInstallProposal = numInstallProposal + peersFromOrg.size();
        responses = client.sendInstallProposal(installProposalRequest, peersFromOrg);

        for (ProposalResponse response : responses) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                out("Successful install proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
                successful.add(response);
            } else {
                failed.add(response);
            }
        }

        SDKUtils.getProposalConsistencySets(responses);
        //   }
        out("Received %d install proposal responses. Successful+verified: %d . Failed: %d", numInstallProposal, successful.size(), failed.size());

        if (failed.size() > 0) {
            ProposalResponse first = failed.iterator().next();
            out("Not enough endorsers for install :" + successful.size() + ".  " + first.getMessage());
            throw new NotEnoughEndorsersException();
        }
        return chaincodeId;
    }

    private void registerAndEnrollAdmin(SampleOrg sampleOrg) throws EnrollmentException, InvalidArgumentException {
        HFCAClient ca = sampleOrg.getCAClient();
        final String orgName = sampleOrg.getName();
        final String mspid = sampleOrg.getMSPID();
        ca.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        SampleUser admin = sampleStore.getMember(TEST_ADMIN_NAME, orgName);
        if (!admin.isEnrolled()) {  //Preregistered admin only needs to be enrolled with Fabric caClient.
            admin.setEnrollment(ca.enroll(admin.getName(), "adminpw"));
            admin.setMspId(mspid);
        }

        sampleOrg.setAdmin(admin);
    }

    private void registerAndEnrollUser(SampleOrg sampleOrg) throws Exception {
        SampleUser user = sampleStore.getMember(TESTUSER_1_NAME, sampleOrg.getName());
        if (!user.isRegistered()) {  // users need to be registered AND enrolled
            try {
                RegistrationRequest rr = new RegistrationRequest(user.getName(), "org1.department1");
                user.setEnrollmentSecret(sampleOrg.getCAClient().register(rr, sampleOrg.getAdmin()));
            } catch (RegistrationException e) {
                out("User Already Registered. Skipping To Enrollment.");
            }
        }
        if (!user.isEnrolled()) {
            try {
                user.setEnrollment(sampleOrg.getCAClient().enroll(user.getName(), user.getEnrollmentSecret()));
                user.setMspId(sampleOrg.getMSPID());
            } catch (EnrollmentException e) {
                out("User Already Enrolled.");
            }
        }
        sampleOrg.addUser(user); //Remember user belongs to this Org

    }

    private void setPeerOrgAdmin(SampleOrg sampleOrg) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        final String sampleOrgName = sampleOrg.getName();
        final String sampleOrgDomainName = sampleOrg.getDomainName();

        SampleUser peerOrgAdmin = sampleStore.getMember(sampleOrgName + "Admin", sampleOrgName, sampleOrg.getMSPID(),
                Util.findFileSk(Paths.get(config.getTestChannelPath(), "crypto-config/peerOrganizations/",
                        sampleOrgDomainName, format("/users/Admin@%s/msp/keystore", sampleOrgDomainName)).toFile()),
                Paths.get(config.getTestChannelPath(), "crypto-config/peerOrganizations/", sampleOrgDomainName,
                        format("/users/Admin@%s/msp/signcerts/Admin@%s-cert.pem", sampleOrgDomainName, sampleOrgDomainName)).toFile());

        //A special user that can create channels, join peers and install chaincode
        sampleOrg.setPeerAdmin(peerOrgAdmin);
    }

    private void populateOrderers() throws org.hyperledger.fabric.sdk.exception.InvalidArgumentException {
        orderers = new LinkedList<>();

        for (String orderName : sampleOrg.getOrdererNames()) {

            Properties ordererProperties = config.getOrdererProperties(orderName);
            ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[] {5L, TimeUnit.MINUTES});
            ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[] {8L, TimeUnit.SECONDS});

            orderers.add(client.newOrderer(orderName, sampleOrg.getOrdererLocation(orderName),
                    ordererProperties));
        }
    }

    private Channel constructChannel(String name) throws Exception {

        out("Constructing channel %s", name);

        //Just pick the first orderer in the list to create the channel.

        Orderer anOrderer = orderers.iterator().next();
        Set<String> channels = client.queryChannels(sampleOrg.getPeers().iterator().next());
        Channel newChannel;
        if (!channels.contains(name)) {
            ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(TEST_FIXTURES_PATH + "/channel-artifacts/" + "channel.tx"));
            //Create channel that has only one signer that is this orgs peer admin. If channel creation policy needed more signature they would need to be added too.
            newChannel = client.newChannel(name, anOrderer, channelConfiguration, client.getChannelConfigurationSignature(channelConfiguration, sampleOrg.getPeerAdmin()));
        } else {
            newChannel = client.newChannel(CHANNEL_NAME);
            newChannel.addOrderer(anOrderer);
        }

        makePeerJoinChannel(newChannel);

        for (Orderer orderer : orderers) {
            newChannel.addOrderer(orderer);
        }

        attachEventToChannel(newChannel);


        newChannel.initialize();

        out("Finished initialization channel %s", name);

        return newChannel;

    }

    private void attachEventToChannel(Channel newChannel) throws org.hyperledger.fabric.sdk.exception.InvalidArgumentException {
        for (String eventHubName : sampleOrg.getEventHubNames()) {

            final Properties eventHubProperties = config.getEventHubProperties(eventHubName);

            eventHubProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[] {5L, TimeUnit.MINUTES});
            eventHubProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[] {8L, TimeUnit.SECONDS});

            EventHub eventHub = client.newEventHub(eventHubName, sampleOrg.getEventHubLocation(eventHubName),
                    eventHubProperties);
            newChannel.addEventHub(eventHub);
        }
    }

    private void makePeerJoinChannel(Channel newChannel) throws org.hyperledger.fabric.sdk.exception.InvalidArgumentException, ProposalException {
        for (Peer peer : sampleOrg.getPeers()) {
            try {
                newChannel.joinPeer(peer);
            } catch (Exception e) {
                out("Peer already Joined");
                newChannel.addPeer(peer);
            }
        }
    }

    public Map<String, Object> queryChannelForChaincodes() throws ProposalException, org.hyperledger.fabric.sdk.exception.InvalidArgumentException {
        List<Query.ChaincodeInfo> chainCodeList = kycChannel.queryInstantiatedChaincodes(sampleOrg.getPeers().iterator().next());
        Map<String, Object> resultMap = new HashMap<>();
        if (chainCodeList.size() != 0) {
            List<ChainCodeIDPojo> chainCodeIDPojos = new LinkedList<>();
            chainCodeList.forEach(chaincodeInfo -> chainCodeIDPojos.add(ChainCodeIDPojo.convertChaincodeInfoToChaincodeIDPojo(chaincodeInfo)));
            resultMap.put("size", chainCodeIDPojos.size());
            resultMap.put(RESULT_KEY, chainCodeIDPojos);
        } else {
            resultMap.put("size", 0);
        }
        return resultMap;
    }

    public Map<String, Object> insertIntoLedger(APIRequest request) throws InvalidNumberArgumentException, IOException, org.hyperledger.fabric.sdk.exception.InvalidArgumentException, ProposalException, NotEnoughEndorsersException, InconsistentProposalResponseException, ExecutionException, InterruptedException {
        if (request != null && request.getRequestParams() != null && request.getRequestParams().getCtorMsg() != null) {
            if (request.getRequestParams().getCtorMsg().getArgs() == null || request.getRequestParams().getCtorMsg().getArgs().size() != 1) {
                throw new InvalidNumberArgumentException(1, request.getRequestParams().getCtorMsg().getArgs() == null ? 0 : request.getRequestParams().getCtorMsg().getArgs().size());
            }
            KYCUser user = KYCUser.toKYCUser(request.getRequestParams().getCtorMsg().getArgs().get(0));
            TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
            transactionProposalRequest.setChaincodeID(kycChaincodeId);
            transactionProposalRequest.setFcn("invoke");
            transactionProposalRequest.setProposalWaitTime(config.getProposalWaitTime());
            transactionProposalRequest.setArgs(new String[] {INSERT_METHOD_KEY, KYCUser.toJsonString(user)});

            Map<String, byte[]> tm2 = new HashMap<>();
            tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
            tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));

            transactionProposalRequest.setTransientMap(tm2);

            return processTransactionRequest(transactionProposalRequest);
        } else {
            throw new BadRequestException();
        }
    }

    public Map<String, Object> queryDataFromLedger(APIRequest request) throws InvalidNumberArgumentException, org.hyperledger.fabric.sdk.exception.InvalidArgumentException, ProposalException, FailedQueryProposalException, QueryResultFailureException {
        if (request.getRequestParams().getCtorMsg().getArgs() == null || request.getRequestParams().getCtorMsg().getArgs().size() != 1) {
            throw new InvalidNumberArgumentException(1, request.getRequestParams().getCtorMsg().getArgs() == null ? 0: request.getRequestParams().getCtorMsg().getArgs().size());
        }

        String aadharNumber = request.getRequestParams().getCtorMsg().getArgs().get(0);

        QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
        queryByChaincodeRequest.setFcn("invoke");
        queryByChaincodeRequest.setArgs(new String[]{RETRIEVE_METHOD_KEY,aadharNumber});
        queryByChaincodeRequest.setChaincodeID(kycChaincodeId);
        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
        queryByChaincodeRequest.setTransientMap(tm2);

        return processQueryRequest(queryByChaincodeRequest);
    }

    public Map<String, Object> historyDataFromLedger(APIRequest request) throws InvalidNumberArgumentException, org.hyperledger.fabric.sdk.exception.InvalidArgumentException, FailedQueryProposalException, ProposalException, QueryResultFailureException {
        if (request.getRequestParams().getCtorMsg().getArgs() == null || request.getRequestParams().getCtorMsg().getArgs().size() != 1) {
            throw new InvalidNumberArgumentException(1, request.getRequestParams().getCtorMsg().getArgs() == null ? 0: request.getRequestParams().getCtorMsg().getArgs().size());
        }

        String aadharNumber = request.getRequestParams().getCtorMsg().getArgs().get(0);

        QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
        queryByChaincodeRequest.setFcn("invoke");
        queryByChaincodeRequest.setArgs(new String[] {HISTORY_METHOD_KEY, aadharNumber});
        queryByChaincodeRequest.setChaincodeID(kycChaincodeId);
        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));

        queryByChaincodeRequest.setTransientMap(tm2);

        return processQueryRequest(queryByChaincodeRequest);
    }

    public Map<String, Object> updateDataIntoLedger(APIRequest request) throws InvalidNumberArgumentException, IOException, org.hyperledger.fabric.sdk.exception.InvalidArgumentException, InterruptedException, ExecutionException, InconsistentProposalResponseException, ProposalException, NotEnoughEndorsersException {
        if (request.getRequestParams().getCtorMsg().getArgs() == null || request.getRequestParams().getCtorMsg().getArgs().size() != 2) {
            throw new InvalidNumberArgumentException(2, request.getRequestParams().getCtorMsg().getArgs() == null ? 0: request.getRequestParams().getCtorMsg().getArgs().size());
        }

        String aadharNumber = request.getRequestParams().getCtorMsg().getArgs().get(0);

        KYCUser user = KYCUser.toKYCUser(request.getRequestParams().getCtorMsg().getArgs().get(1));

        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(kycChaincodeId);
        transactionProposalRequest.setFcn("invoke");
        transactionProposalRequest.setProposalWaitTime(config.getProposalWaitTime());
        transactionProposalRequest.setArgs(new String[] {UPDATE_METHOD_KEY, aadharNumber, KYCUser.toJsonString(user)});

        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));

        transactionProposalRequest.setTransientMap(tm2);

        return processTransactionRequest(transactionProposalRequest);
    }

    public Map<String, Object> insertPropertyIntoLedger(APIRequest request) throws IOException, org.hyperledger.fabric.sdk.exception.InvalidArgumentException, InterruptedException, ExecutionException, InconsistentProposalResponseException, ProposalException, NotEnoughEndorsersException, InvalidNumberArgumentException, QueryResultFailureException {
        if (request.getRequestParams().getCtorMsg().getArgs() == null || request.getRequestParams().getCtorMsg().getArgs().size() != 1) {
            throw new InvalidNumberArgumentException(1, request.getRequestParams().getCtorMsg().getArgs() == null ? 0 : request.getRequestParams().getCtorMsg().getArgs().size());
        }

        Asset asset = Asset.toAsset(request.getRequestParams().getCtorMsg().getArgs().get(0));

        APIRequest apiRequest = APIRequest.buildNewRequest(Arrays.asList(asset.getOwner()));
        try {
            queryDataFromLedger(apiRequest);
        } catch (Exception e) {
            throw new QueryResultFailureException(e.getMessage());
        }

        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(propertyChaincodeId);
        transactionProposalRequest.setFcn("invoke");
        transactionProposalRequest.setProposalWaitTime(config.getProposalWaitTime());
        transactionProposalRequest.setArgs(new String[] {INSERT_ASSET_METHOD_KEY, Asset.toJsonString(asset)});

        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));

        transactionProposalRequest.setTransientMap(tm2);

        return processTransactionRequest(transactionProposalRequest);
    }

    public Map<String, Object> getBlockByNumber(APIRequest request) throws InvalidNumberArgumentException, ProposalException, org.hyperledger.fabric.sdk.exception.InvalidArgumentException, IOException {
        if (request.getRequestParams().getCtorMsg().getArgs() == null || request.getRequestParams().getCtorMsg().getArgs().size() != 1) {
            throw new InvalidNumberArgumentException(1, request.getRequestParams().getCtorMsg().getArgs() == null ? 0: request.getRequestParams().getCtorMsg().getArgs().size());
        }

        Long blockNumber = Long.valueOf(request.getRequestParams().getCtorMsg().getArgs().get(0));

        BlockInfo blockInfo = kycChannel.queryBlockByNumber(blockNumber);
        Map<String, Object> stringObjectMap = new HashMap<>();
        Map<String, Object> block = new HashMap<>();
        Map<String, Object> data = new HashMap<>();

        List<String> str = new ArrayList<>();

        for (ByteString bytes : blockInfo.getBlock().getData().getDataList()) {
            str.add(bytes.toStringUtf8());
        }

        data.put("data", str);
        block.put("currentBlockHash", Hex.encodeHexString(blockInfo.getBlock().getHeader().getDataHash().toByteArray()));
        block.put("previousBlockHash", Hex.encodeHexString(blockInfo.getBlock().getHeader().getPreviousHash().toByteArray()));
        block.put("dataBlock", data);

        stringObjectMap.put(RESULT_KEY, block);

        return stringObjectMap;
    }

    public Map<String, Object> getPropertyInfoById(APIRequest request) throws InvalidNumberArgumentException, org.hyperledger.fabric.sdk.exception.InvalidArgumentException, FailedQueryProposalException, ProposalException, QueryResultFailureException {
        if (request.getRequestParams().getCtorMsg().getArgs() == null || request.getRequestParams().getCtorMsg().getArgs().size() != 1) {
            throw new InvalidNumberArgumentException(1, request.getRequestParams().getCtorMsg().getArgs() == null ? 0: request.getRequestParams().getCtorMsg().getArgs().size());
        }

        String propertyId = request.getRequestParams().getCtorMsg().getArgs().get(0);

        QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
        queryByChaincodeRequest.setFcn("invoke");
        queryByChaincodeRequest.setArgs(new String[]{GET_PROPERTY_ID_METHOD_KEY, propertyId});
        queryByChaincodeRequest.setChaincodeID(propertyChaincodeId);
        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
        queryByChaincodeRequest.setTransientMap(tm2);

        return processQueryRequest(queryByChaincodeRequest);
    }

    public Map<String, Object> getPropertyInfoByOwner(APIRequest request) throws InvalidNumberArgumentException, org.hyperledger.fabric.sdk.exception.InvalidArgumentException, FailedQueryProposalException, ProposalException, QueryResultFailureException {
        if (request.getRequestParams().getCtorMsg().getArgs() == null || request.getRequestParams().getCtorMsg().getArgs().size() != 1) {
            throw new InvalidNumberArgumentException(1, request.getRequestParams().getCtorMsg().getArgs() == null ? 0: request.getRequestParams().getCtorMsg().getArgs().size());
        }

        String ownerId = request.getRequestParams().getCtorMsg().getArgs().get(0);

        QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
        queryByChaincodeRequest.setFcn("invoke");
        queryByChaincodeRequest.setArgs(new String[]{GET_PROPERTY_OWNER_METHOD_KEY, ownerId});
        queryByChaincodeRequest.setChaincodeID(propertyChaincodeId);
        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
        queryByChaincodeRequest.setTransientMap(tm2);

        return processQueryRequest(queryByChaincodeRequest);
    }

    public Map<String, Object> propertyTransferRequest(APIRequest request) throws InvalidNumberArgumentException, org.hyperledger.fabric.sdk.exception.InvalidArgumentException, FailedQueryProposalException, ProposalException, QueryResultFailureException, InterruptedException, ExecutionException, InconsistentProposalResponseException, NotEnoughEndorsersException, UnsupportedEncodingException {
        if (request.getRequestParams().getCtorMsg().getArgs() == null || request.getRequestParams().getCtorMsg().getArgs().size() != 3) {
            throw new InvalidNumberArgumentException(3, request.getRequestParams().getCtorMsg().getArgs() == null ? 0: request.getRequestParams().getCtorMsg().getArgs().size());
        }

        String currOwnerId = request.getRequestParams().getCtorMsg().getArgs().get(0);
        String propertyId = request.getRequestParams().getCtorMsg().getArgs().get(1);
        String newOwnerId = request.getRequestParams().getCtorMsg().getArgs().get(2);



        try {
            APIRequest apiRequest = APIRequest.buildNewRequest(Arrays.asList(currOwnerId));
            Map<String, Object> stringObjectMap = queryDataFromLedger(apiRequest);
            if (!stringObjectMap.containsKey("result")) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new QueryResultFailureException(String.format("%s not a valid User.", currOwnerId));
        }

        try {
            APIRequest apiRequest = APIRequest.buildNewRequest(Arrays.asList(propertyId));
            Map<String, Object> stringObjectMap = getPropertyInfoById(apiRequest);
            if (!stringObjectMap.containsKey("result")) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new QueryResultFailureException(String.format("%s not a valid Property.", propertyId));
        }

        try {
            APIRequest apiRequest = APIRequest.buildNewRequest(Arrays.asList(newOwnerId));
            Map<String, Object> stringObjectMap = queryDataFromLedger(apiRequest);
            if (!stringObjectMap.containsKey("result")) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new QueryResultFailureException(String.format("%s not a valid User.", newOwnerId));
        }

        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setFcn("invoke");
        transactionProposalRequest.setArgs(new String[]{TRANSFER_PROPERTY_METHOD_KEY, propertyId, currOwnerId,newOwnerId});
        transactionProposalRequest.setChaincodeID(propertyChaincodeId);

        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
        transactionProposalRequest.setTransientMap(tm2);

        return processTransactionRequest(transactionProposalRequest);
    }

    public Map<String, Object> acceptPropertyTransferRequest(APIRequest request) throws InvalidNumberArgumentException, org.hyperledger.fabric.sdk.exception.InvalidArgumentException, FailedQueryProposalException, ProposalException, InterruptedException, ExecutionException, InconsistentProposalResponseException, NotEnoughEndorsersException, UnsupportedEncodingException {
        if (request.getRequestParams().getCtorMsg().getArgs() == null || request.getRequestParams().getCtorMsg().getArgs().size() != 2) {
            throw new InvalidNumberArgumentException(2, request.getRequestParams().getCtorMsg().getArgs() == null ? 0: request.getRequestParams().getCtorMsg().getArgs().size());
        }
        String newOwnerId = request.getRequestParams().getCtorMsg().getArgs().get(0);
        String propertyId = request.getRequestParams().getCtorMsg().getArgs().get(1);

        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setFcn("invoke");
        transactionProposalRequest.setArgs(new String[]{ACCEPT_TRANSFER_PROPERTY_METHOD_KEY, newOwnerId, propertyId});
        transactionProposalRequest.setChaincodeID(propertyChaincodeId);
        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
        transactionProposalRequest.setTransientMap(tm2);

        return processTransactionRequest(transactionProposalRequest);
    }

    public Map<String, Object> approveTransferRequest(APIRequest request) throws InvalidNumberArgumentException, org.hyperledger.fabric.sdk.exception.InvalidArgumentException, InconsistentProposalResponseException, NotEnoughEndorsersException, ExecutionException, UnsupportedEncodingException, InterruptedException, ProposalException {
        if (request.getRequestParams().getCtorMsg().getArgs() == null || request.getRequestParams().getCtorMsg().getArgs().size() != 1) {
            throw new InvalidNumberArgumentException(1, request.getRequestParams().getCtorMsg().getArgs() == null ? 0: request.getRequestParams().getCtorMsg().getArgs().size());
        }

        String propertyId = request.getRequestParams().getCtorMsg().getArgs().get(0);

        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setFcn("invoke");
        transactionProposalRequest.setArgs(new String[]{APPROVE_TRANSFER_PROPERTY_METHOD_KEY, propertyId});
        transactionProposalRequest.setChaincodeID(propertyChaincodeId);
        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
        transactionProposalRequest.setTransientMap(tm2);

        return processTransactionRequest(transactionProposalRequest);
    }

    private Map<String, Object> processTransactionRequest(TransactionProposalRequest request) throws NotEnoughEndorsersException, org.hyperledger.fabric.sdk.exception.InvalidArgumentException, ProposalException, InconsistentProposalResponseException, ExecutionException, InterruptedException, UnsupportedEncodingException {
        Collection<ProposalResponse> transactionProposalResponse = kycChannel.sendTransactionProposal(request, kycChannel.getPeers());

        for (ProposalResponse response : transactionProposalResponse) {
            if (response.getStatus() == ProposalResponse.Status.FAILURE) {
                throw new NotEnoughEndorsersException();
            }
        }

        Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(transactionProposalResponse);
        if (proposalConsistencySets.size() != 1) {
            throw new InconsistentProposalResponseException();
        }

        out("Successfully received transaction proposal responses.");
        out("Sending chaincode transaction to orderer.");

        byte[] x = kycChannel.sendTransaction(transactionProposalResponse).get().getTransactionActionInfo(0).getProposalResponsePayload();
        String resultAsString = null;
        if (x != null) {
            resultAsString = new String(x, "UTF-8");
        }


        String transactionId = transactionProposalResponse.iterator().next().getTransactionID();
        BlockInfo returnedBlock = kycChannel.queryBlockByTransactionID(transactionId);

        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put(RESULT_KEY, resultAsString);
        stringObjectMap.put(TRANSACTION_ID_KEY, transactionId);
        stringObjectMap.put(BLOCK_NUMBER_KEY, returnedBlock.getBlockNumber());
        return stringObjectMap;
    }

    private Map<String, Object> processQueryRequest(QueryByChaincodeRequest request) throws FailedQueryProposalException, ProposalException, org.hyperledger.fabric.sdk.exception.InvalidArgumentException, QueryResultFailureException {
        Collection<ProposalResponse> queryProposalResponse = kycChannel.queryByChaincode(request, kycChannel.getPeers());
        for (ProposalResponse response: queryProposalResponse) {
            if (!response.isVerified() || response.getStatus() != ProposalResponse.Status.SUCCESS) {
                throw new FailedQueryProposalException(response.getPeer(), response.isVerified(), response.getStatus(), response.getMessage());
            }
        }

        String payload = queryProposalResponse.iterator().next().getProposalResponse().getResponse().getPayload().toStringUtf8();
        String transactionId = queryProposalResponse.iterator().next().getTransactionID();
        ChaincodeResponse.Status status = queryProposalResponse.iterator().next().getStatus();

        if (status == ChaincodeResponse.Status.FAILURE) {
            throw new QueryResultFailureException(queryProposalResponse.iterator().next().getMessage());
        }

        if (payload == null || payload.length() == 0) {
            throw new QueryResultFailureException(String.format("No Such Object With Key : %s", request.getArgs().get(1)));
        }

        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put(RESULT_KEY, payload);
        stringObjectMap.put(TRANSACTION_ID_KEY, transactionId);

        return stringObjectMap;
    }
}
