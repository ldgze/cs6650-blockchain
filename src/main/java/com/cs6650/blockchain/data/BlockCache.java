package com.cs6650.blockchain.data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.java_websocket.WebSocket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * A cache for blockchain-related data.
 */
@ConfigurationProperties(prefix = "block")
@Component
public class BlockCache {

	/**
	 * The list of blocks in the blockchain for this node.
	 */
	private List<Block> blockChain = new CopyOnWriteArrayList<Block>();

	/**
	 * The list of transactions that have been packed and saved.
	 */
	private List<Transaction> packedTransactions = new CopyOnWriteArrayList<Transaction>();
	
	/**
	 * The list of sockets for this node.
	 */
	private List<WebSocket> socketsList = new CopyOnWriteArrayList<WebSocket>();
	
	/**
	 * The difficulty of the proof-of-work algorithm used for mining.
	 */
	@Value("${block.difficulty}")
	private int difficulty;
	
	/**
	 * The port number for the P2P server on this node.
	 */
	@Value("${block.p2pport}")
	private int p2pport;
	
	/**
	 * The string of addresses of the nodes to connect to.
	 */
	@Value("${block.address}")
	private String address;

	/**
	 * Returns the latest block in the blockchain for this node.
	 *
	 * @return The latest block in the blockchain, or null if the blockchain is empty.
	 */
	public Block getLatestBlock() {
		return blockChain.size() > 0 ? blockChain.get(blockChain.size() - 1) : null;
	}

	public List<Block> getBlockChain() {
		return blockChain;
	}

	public void setBlockChain(List<Block> blockChain) {
		this.blockChain = blockChain;
	}

	public List<Transaction> getPackedTransactions() {
		return packedTransactions;
	}

	public void setPackedTransactions(List<Transaction> packedTransactions) {
		this.packedTransactions = packedTransactions;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}

	public List<WebSocket> getSocketsList() {
		return socketsList;
	}

	public void setSocketsList(List<WebSocket> socketsList) {
		this.socketsList = socketsList;
	}

	public int getP2pport() {
		return p2pport;
	}

	public void setP2pport(int p2pport) {
		this.p2pport = p2pport;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String addresses) {
		this.address = addresses;
	}

	public String[] getAddressList() {
		return address.split(" ");
	}

}
