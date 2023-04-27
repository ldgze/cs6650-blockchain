package com.cs6650.blockchain.data;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents the block structure in a blockchain system.
 */
public class Block implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * the index number or height of the block
	 */
	private int index;
	/**
	 * the unique identifier of the current block
	 */
	private String hash;
	/**
	 * the hash value of the previous block in the chain
	 */
	private String hashPrevious;
	/**
	 * the time when the block was created
	 */
	private long timestamp;
	/**
	 * the number of times the correct hash value was calculated
	 */
	private int nonce;
	/**
	 * a list of business data stored in the block, such as transaction information, bill information, contract information, etc
	 */
	private List<Transaction> transactions;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public int getNonce() {
		return nonce;
	}

	public void setNonce(int nonce) {
		this.nonce = nonce;
	}

	public String getHashPrevious() {
		return hashPrevious;
	}

	public void setHashPrevious(String hashPrevious) {
		this.hashPrevious = hashPrevious;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
}
