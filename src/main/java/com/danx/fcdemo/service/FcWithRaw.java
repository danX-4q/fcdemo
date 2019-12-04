package com.danx.fcdemo.service;

import java.io.File;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.Peer.PeerRole;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.SDKUtils;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import static org.hyperledger.fabric.sdk.Channel.PeerOptions.createPeerOptions;

/*
 * main code from:
 *   https://stackoverflow.com/questions/51607992/fabric-sdk-java-invoke-chaincode-doesnt-work
 */

/*
 * refer[more simple code]:
 *   https://www.jianshu.com/p/3d61e8c46f43
 */

/*
 * refer[submit tx async by BlockEvent.TransactionEvent]
 *   https://blog.csdn.net/weixin_33729196/article/details/92840957
	try{
		BlockEvent.TransactionEvent event =  org.getChannel().get().sendTransaction(successful).get(180, TimeUnit.SECONDS);
		if (event.isValid()) {
			System.out.println("tx success");
			resultMap.put("code", "success");
		} else {
			System.out.println("tx failed");
			resultMap.put("code", "transaction_no");
		}
	}catch (Exception e){
		System.err.println("IntermediateChaincodeID==>toOrdererResponse==>Exception:"+e.getMessage());
		resultMap.put("code", "error");
	}
 */

public class FcWithRaw {

	//modify them when filesystem path change!
	final String prefixUserPath = "/Users/danoking/Documents/workspace/fabric/fabric-samples/first-network/crypto-config/peerOrganizations/org11.example.com/users/";
	final String privateKeyFileName = prefixUserPath + "Admin@org11.example.com/msp/keystore/a5eb4508e00be162ad75146dc7d4ac6c19deddedf661e638ba2f4c89660e645f_sk";
	final String certificateFileName = prefixUserPath + "Admin@org11.example.com/msp/signcerts/Admin@org11.example.com-cert.pem";
	final String peer0Org11PemFile = "/Users/danoking/Documents/workspace/fabric/fabric-samples/first-network/crypto-config/peerOrganizations/org11.example.com/peers/peer0.org11.example.com/tls/server.crt";
	final String peer0Org22PemFile = "/Users/danoking/Documents/workspace/fabric/fabric-samples/first-network/crypto-config/peerOrganizations/org22.example.com/peers/peer0.org22.example.com/tls/server.crt";
	final String order0Org11PemFile = "/Users/danoking/Documents/workspace/fabric/fabric-samples/first-network/crypto-config/ordererOrganizations/example.com/orderers/orderer.example.com/tls/server.crt";
	final String tmpStore = "./temp/store";

	////////////////////////////////////

    public void Do() {
		try {

			Security.addProvider(new BouncyCastleProvider());
			final CryptoSuite crypto = CryptoSuite.Factory.getCryptoSuite();

			System.out.println("Creating client");
			final HFClient client = HFClient.createNewInstance();
			client.setCryptoSuite(crypto);

			System.out.println("Loading Admin@org11.example.com from disk");
			final File privateKeyFile = new File(privateKeyFileName);
			final File certificateFile = new File(certificateFileName);

			File file = new File(tmpStore);
			if (file.exists()){
				file.delete();
				file = new File(tmpStore);
				System.out.println(file.getName()+"delete and re-create!");
			} else {
				System.out.println("do nothing.");
			}
			final SampleStore storeInst = new SampleStore(file);

			//final User User1 = Utils.getUser("PeerAdmin", "Org1MSP", privateKeyFile, certificateFile);
			final User User1 = storeInst.getMember(
									"Admin", "Org11", "Org11MSP", 
									privateKeyFile, certificateFile
								);
			client.setUserContext(User1);

 			// Accessing channel, should already exist
			System.out.println("Accessing channel");
			final Channel myChannel = client.newChannel("zrchannel");

			System.out.println("Setting zrchannel configuration");
			final List<Peer> peers = new LinkedList<>();

			{
				final Properties peerProperties = new Properties();
				peerProperties.setProperty("pemFile", peer0Org11PemFile);
				peerProperties.setProperty("hostnameOverride", "peer0.org11.example.com");
				peerProperties.setProperty("sslProvider", "openSSL");
				peerProperties.setProperty("negotiationType", "TLS");
				peerProperties.setProperty("trustServerCertificate", "true"); // testing // // PRODUCTION!
				peerProperties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);
				peers.add(client.newPeer("peer0.org11.example.com", "grpcs://localhost:7051", peerProperties));
			}

			{
				final Properties peerProperties = new Properties();
				peerProperties.setProperty("pemFile", peer0Org22PemFile);
				peerProperties.setProperty("hostnameOverride", "peer0.org22.example.com");
				peerProperties.setProperty("sslProvider", "openSSL");
				peerProperties.setProperty("negotiationType", "TLS");
				peerProperties.setProperty("trustServerCertificate", "true"); // testing // // PRODUCTION!
				peerProperties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);
				peers.add(client.newPeer("peer0.org22.example.com", "grpcs://localhost:9051", peerProperties));
			}

			myChannel.addPeer(peers.get(0), createPeerOptions().setPeerRoles(EnumSet.of(PeerRole.ENDORSING_PEER,
					PeerRole.LEDGER_QUERY, PeerRole.CHAINCODE_QUERY, PeerRole.EVENT_SOURCE)));
			myChannel.addPeer(peers.get(1), createPeerOptions().setPeerRoles(EnumSet.of(PeerRole.ENDORSING_PEER,
			PeerRole.LEDGER_QUERY, PeerRole.CHAINCODE_QUERY, PeerRole.EVENT_SOURCE)));


			final Properties ordererProperties = new Properties();
			ordererProperties.setProperty("pemFile", order0Org11PemFile);
			ordererProperties.setProperty("trustServerCertificate", "true");
			ordererProperties.setProperty("hostnameOverride", "orderer.example.com");
			ordererProperties.setProperty("sslProvider", "openSSL");
			ordererProperties.setProperty("negotiationType", "TLS");
			ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[] { 5L, TimeUnit.MINUTES });
			ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[] { 8L, TimeUnit.SECONDS });
			myChannel.addOrderer(client.newOrderer("orderer.example.com", "grpcs://localhost:7050", ordererProperties));

			myChannel.initialize();

			// Creating proposal for query
			System.out.println("Creating proposal for query(a)");
			final QueryByChaincodeRequest queryAProposalRequest = client.newQueryProposalRequest();
			final ChaincodeID queryChaincodeID = ChaincodeID.newBuilder().setName("mycc").setVersion("1.0").build();
			queryAProposalRequest.setChaincodeID(queryChaincodeID);
			queryAProposalRequest.setFcn("query");
			// queryAProposalRequest.setFcn("queryAllCars");
			queryAProposalRequest.setProposalWaitTime(TimeUnit.SECONDS.toMillis(10));
			queryAProposalRequest.setArgs(new String[] { "a" });
			final int blocksize = (int) myChannel.queryBlockchainInfo().getHeight();
			System.out.println("NO of Blocks: " + blocksize);
			// Send proposal and wait for responses
			System.out.println("Sending proposal for query");
			final Collection<ProposalResponse> queryAResponses = myChannel.queryByChaincode(queryAProposalRequest,
					myChannel.getPeers());
			for (final ProposalResponse resp : queryAResponses) {
				System.out.println(
						"Response from peer " + resp.getPeer().getName() + " is " + resp.getProposalResponse().getResponse()
								+ resp.getProposalResponse().getResponse().getPayload().toStringUtf8());
			}

			// Creating proposal for invoke
			System.out.println("Creating proposal for invoke(a,b,10)");
			final TransactionProposalRequest invokeProposalRequest = client.newTransactionProposalRequest();
			invokeProposalRequest.setChaincodeID(queryChaincodeID);
			invokeProposalRequest.setFcn("invoke");
			invokeProposalRequest.setProposalWaitTime(TimeUnit.SECONDS.toMillis(10));
			invokeProposalRequest.setArgs(new String[] { "a", "b", "10" });

			//Step 1
			final Collection<ProposalResponse> queryBResponses = myChannel.sendTransactionProposal(invokeProposalRequest,
					myChannel.getPeers());
			for (final ProposalResponse resp : queryBResponses) {
				System.out.println("Response from peer " + resp.getPeer().getName() + " is"
						+ resp.getProposalResponse().getResponse());
			}
		
			// STEP 2
			Collection<ProposalResponse> successful = new ArrayList<>();
			Collection<ProposalResponse> failed = new ArrayList<>();
			for (ProposalResponse response : queryBResponses) {
				if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
					successful.add(response);
				} else {
					failed.add(response);
				}
			}
			if (failed.size() > 0) throw new Exception("...");
			// CHECK CONSISTENCY
			Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(queryBResponses);
			if (proposalConsistencySets.size() != 1) {
				throw new Exception("...");
			}
			// STEP 3
			CompletableFuture<BlockEvent.TransactionEvent> transactionEventCompletableFuture = myChannel.sendTransaction(successful);
			transactionEventCompletableFuture.get(180, TimeUnit.SECONDS);
			//myChannel.sendTransaction(successful);
			System.out.println("After: invoke(a,b,10)");
		} catch (Exception e) {
			//TODO: handle exception
			e.printStackTrace();
		}
    }
}