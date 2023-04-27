package com.cs6650.blockchain.p2p;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.cs6650.blockchain.data.Block;
import com.cs6650.blockchain.data.BlockService;
import com.cs6650.blockchain.util.BlockConstant;
import org.java_websocket.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.cs6650.blockchain.data.BlockCache;

/**
 * P2P network service class
 *
 */
@Service
public class P2PService implements ApplicationRunner {
	
	@Autowired
	BlockService blockService;
	
	@Autowired
	BlockCache blockCache;
	
	@Autowired
	P2PServer p2PServer;
	
	@Autowired
	P2PClient p2PClient;

	/**
	 * Message handling method shared by the client and server
	 * @param webSocket
	 * @param msg
	 * @param sockets
	 */
	public void handleMessage(WebSocket webSocket, String msg, List<WebSocket> sockets) {
		try {
			P2PMessage message = JSON.parseObject(msg, P2PMessage.class);
			System.out.println("Received p2p message from IP address: " +webSocket.getRemoteSocketAddress().getAddress().toString()
					+", port number: "+ webSocket.getRemoteSocketAddress().getPort() + "："
			        + JSON.toJSONString(message));
			switch (message.getType()) {
			// Client requests to query the latest block: 1
			case BlockConstant.QUERY_LATEST_BLOCK:
				write(webSocket, responseLatestBlockMsg());// Server calls the method to return the latest block: 2
				break;
			// Received the latest block returned by the server: 2
			case BlockConstant.RESPONSE_LATEST_BLOCK:
				handleBlockResponse(message.getData(), sockets);
				break;
			// Client requests to query the entire blockchain: 3
			case BlockConstant.QUERY_BLOCKCHAIN:
				write(webSocket, responseBlockChainMsg());// Server calls the method to return the latest block: 4
				break;
			// Received the entire blockchain information sent by other nodes directly: 4
			case BlockConstant.RESPONSE_BLOCKCHAIN:
				handleBlockChainResponse(message.getData(), sockets);
				break;
			}
		} catch (Exception e) {
			System.out.println("Error handling p2p message from IP address: " +webSocket.getRemoteSocketAddress().getAddress().toString()
				+", port number: "+ webSocket.getRemoteSocketAddress().getPort() + ": "
				+ e.getMessage());
		}
	}

	/**
	 * Handle the block information sent by other nodes
	 * @param blockData
	 * @param sockets
	 */
	public synchronized void handleBlockResponse(String blockData, List<WebSocket> sockets) {
		// Deserialize to get the latest block information from other nodes
		Block latestBlockReceived = JSON.parseObject(blockData, Block.class);
		// The latest block of the current node
		Block latestBlock = blockCache.getLatestBlock();
		
		if (latestBlockReceived != null) {
			if(latestBlock != null) {
				// If the height of the received block is much greater than the local block height
				if(latestBlockReceived.getIndex() > latestBlock.getIndex() + 1) {
					broatcast(queryBlockChainMsg());
					System.out.println("Re-query the entire blockchain on all nodes");
				}else if (latestBlockReceived.getIndex() > latestBlock.getIndex() && 
						latestBlock.getHash().equals(latestBlockReceived.getHashPrevious())) {
					if (blockService.addBlock(latestBlockReceived)) {
						broatcast(responseLatestBlockMsg());
					}
					System.out.println("Add the newly received block to the local blockchain");
				}
			}else if(latestBlock == null) {
				broatcast(queryBlockChainMsg());
				System.out.println("Re-query the entire blockchain on all nodes");
			}
		}
	}
	
	/**
	 * Handle the blockchain information sent by other nodes
	 * @param blockData
	 * @param sockets
	 */
	public synchronized void handleBlockChainResponse(String blockData, List<WebSocket> sockets) {
		// Deserialize to get the entire blockchain information from other nodes
		List<Block> receiveBlockchain = JSON.parseArray(blockData, Block.class);
		if(!CollectionUtils.isEmpty(receiveBlockchain) && blockService.isValidChain(receiveBlockchain)) {
			// Sort the blocks by index
			Collections.sort(receiveBlockchain, new Comparator<Block>() {
				public int compare(Block block1, Block block2) {
					return block1.getIndex() - block2.getIndex();
				}
			});
			
			// The latest block of other nodes
			Block latestBlockReceived = receiveBlockchain.get(receiveBlockchain.size() - 1);
			// The latest block of the current node
			Block latestBlock = blockCache.getLatestBlock();
			
			if(latestBlock == null) {
				// Replace the local blockchain
				blockService.replaceChain(receiveBlockchain);
			}else {
				// If the other node's blockchain is longer than the current node's, process the current node's blockchain
				if (latestBlockReceived.getIndex() > latestBlock.getIndex()) {
					if (latestBlock.getHash().equals(latestBlockReceived.getHashPrevious())) {
						if (blockService.addBlock(latestBlockReceived)) {
							broatcast(responseLatestBlockMsg());
						}
						System.out.println("Add the newly received block to the local blockchain");
					} else {
						// Replace the local short chain with the longer chain
						blockService.replaceChain(receiveBlockchain);
					}
				}
			}
		}
	}
	
	/**
	 * Broadcast a message across the network
	 * @param message
	 */
	public void broatcast(String message) {
		List<WebSocket> socketsList = this.getSockets();
		if (CollectionUtils.isEmpty(socketsList)) {
			return;
		}
		System.out.println("======Broadcast message start: ");
		for (WebSocket socket : socketsList) {
			this.write(socket, message);
		}
		System.out.println("======Broadcast message end");
	}
	
	/**
	 * Send a message to other nodes
	 * @param ws
	 * @param message
	 */
	public void write(WebSocket ws, String message) {
		System.out.println("Send p2p message to IP address: " +ws.getRemoteSocketAddress().getAddress().toString()
			+ ", port number: "+ws.getRemoteSocketAddress().getPort() + ": " + message);
		ws.send(message);
	}

	/**
	 * Query the entire blockchain
	 * @return
	 */
	public String queryBlockChainMsg() {
		return JSON.toJSONString(new P2PMessage(BlockConstant.QUERY_BLOCKCHAIN));
	}
	
	/**
	 * Return the entire blockchain data
	 * @return
	 */
	public String responseBlockChainMsg() {
		P2PMessage msg = new P2PMessage();
		msg.setType(BlockConstant.RESPONSE_BLOCKCHAIN);
		msg.setData(JSON.toJSONString(blockCache.getBlockChain()));
		return JSON.toJSONString(msg);
	}

	/**
	 * Query the latest block
	 * @return
	 */
	public String queryLatestBlockMsg() {
		return JSON.toJSONString(new P2PMessage(BlockConstant.QUERY_LATEST_BLOCK));
	}
	
	/**
	 * Return the latest block
	 * @return
	 */
	public String responseLatestBlockMsg() {
		P2PMessage msg = new P2PMessage();
		msg.setType(BlockConstant.RESPONSE_LATEST_BLOCK);
		Block b = blockCache.getLatestBlock();
		msg.setData(JSON.toJSONString(b));
		return JSON.toJSONString(msg);
	}
	
	public List<WebSocket> getSockets(){
		return blockCache.getSocketsList();
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		p2PServer.initP2PServer(blockCache.getP2pport());
		for (String address: blockCache.getAddressList()) {
			p2PClient.connectToPeer(address);
		}
		System.out.println("*****Difficulty: "+blockCache.getDifficulty()+"******");
		System.out.println("*****Port number: "+blockCache.getP2pport()+"******");
		for (String address: blockCache.getAddressList()) {
			System.out.println("*****Node address: "+address+"******");
		}
		
	}
	
}
