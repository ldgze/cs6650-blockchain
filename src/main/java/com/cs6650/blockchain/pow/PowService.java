package com.cs6650.blockchain.pow;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.cs6650.blockchain.p2p.P2PService;
import com.cs6650.blockchain.data.Block;
import com.cs6650.blockchain.p2p.P2PMessage;
import com.cs6650.blockchain.data.Transaction;
import com.cs6650.blockchain.data.BlockService;
import com.cs6650.blockchain.util.BlockConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.cs6650.blockchain.data.BlockCache;

/**
 * Consensus mechanism
 * Using POW (Proof of Work) for consensus implementation
 *
 */
@Service
public class PowService {

	@Autowired
	BlockCache blockCache;
	
	@Autowired
	BlockService blockService;
	
	@Autowired
	P2PService p2PService;
	
	/**
	 * Achieve consensus among nodes through "mining" Proof of Work
	 *
	 */
	public Block mine(){
		
		// Encapsulate the business data collection, record the node information where the block is generated, and temporarily hard-code the implementation
		List<Transaction> tsaList = new ArrayList<Transaction>();
		Transaction tsa1 = new Transaction();
		tsa1.setId("1");
		tsa1.setBusinessInfo("This is a block mined by the node with IP: "+ getLocalIp()+", port: "+blockCache.getP2pport());
		tsaList.add(tsa1);
		Transaction tsa2 = new Transaction();
		tsa2.setId("2");
		tsa2.setBusinessInfo("Blockchain height: "+(blockCache.getLatestBlock().getIndex()+1));
		tsaList.add(tsa2);
		
		// Define the result of the hash function for each attempt
		String newBlockHash = "";
		int nonce = 0;
		long start = System.currentTimeMillis();
		System.out.println("Start mining");
		while (true) {
			// Calculate the new block hash value
			newBlockHash = blockService.calculateHash(blockCache.getLatestBlock().getHash(), tsaList, nonce);
			// Verify the hash value
			if (blockService.isValidHash(newBlockHash)) {
				System.out.println("Mining completed, correct hash value: " + newBlockHash);
				System.out.println("Mining time: " + (System.currentTimeMillis() - start) + "ms");
				break;
			}
			System.out.println("Hash value calculated for the "+(nonce+1)+"th attempt: " + newBlockHash);
			nonce++;
		}
		// Create a new block
		Block block = blockService.createNewBlock(nonce, blockCache.getLatestBlock().getHash(), newBlockHash, tsaList);

		// After successful creation, broadcast it to the entire network
		P2PMessage msg = new P2PMessage();
		msg.setType(BlockConstant.RESPONSE_LATEST_BLOCK);
		msg.setData(JSON.toJSONString(block));
		p2PService.broatcast(JSON.toJSONString(msg));
		
		return block;
	}

	/**
	 * Get local IP
	 * @return
	 */
	public static String getLocalIp() {
		try {
			InetAddress ip4 = InetAddress.getLocalHost();
			return ip4.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return "";
	}
	
}
