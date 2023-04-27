package com.cs6650.blockchain.data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

/**
 * Blockchain core service
 *
 */
@Service
public class BlockService {

	@Autowired
	BlockCache blockCache;
	/**
	 * Create genesis block
	 * @return
	 */
	public String createGenesisBlock() {
		Block genesisBlock = new Block();
		// Set the height of the genesis block to 1
		genesisBlock.setIndex(1);
		genesisBlock.setTimestamp(System.currentTimeMillis());
		genesisBlock.setNonce(1);
		// Encapsulate business data
		List<Transaction> tsaList = new ArrayList<Transaction>();
		Transaction tsa = new Transaction();
		tsa.setId("1");
		tsa.setBusinessInfo("This is the genesis block");
		tsaList.add(tsa);
		Transaction tsa2 = new Transaction();
		tsa2.setId("2");
		tsa2.setBusinessInfo("Blockchain height is: 1");
		tsaList.add(tsa2);		
		genesisBlock.setTransactions(tsaList);
		// Set the hash value of the genesis block
		genesisBlock.setHash(calculateHash("",tsaList,1));
		// Add to the collection of packaged saved business data
		blockCache.getPackedTransactions().addAll(tsaList);
		// Add to the blockchain
		blockCache.getBlockChain().add(genesisBlock);
		return JSON.toJSONString(genesisBlock);
	}
	
	/**
	 * Create a new block
	 * @param nonce
	 * @param previousHash
	 * @param hash
	 * @param blockTxs
	 * @return
	 */
	public Block createNewBlock(int nonce, String previousHash, String hash, List<Transaction> blockTxs) {
		Block block = new Block();
		block.setIndex(blockCache.getBlockChain().size() + 1);
		// Timestamp
		block.setTimestamp(System.currentTimeMillis());
		block.setTransactions(blockTxs);
		// Proof of work, the number of times the correct hash value is calculated
		block.setNonce(nonce);
		// Previous block's hash
		block.setHashPrevious(previousHash);
		// Current block's hash
		block.setHash(hash);
		if (addBlock(block)) {
			return block;
		}
		return null;
	}

	/**
	 * Add a new block to the current node's blockchain
	 * 
	 * @param newBlock
	 */
	public boolean addBlock(Block newBlock) {
		// First, verify the legitimacy of the new block
		if (isValidNewBlock(newBlock, blockCache.getLatestBlock())) {
			blockCache.getBlockChain().add(newBlock);
			// The business data of the new block needs to be added to the packaged transaction collection
			blockCache.getPackedTransactions().addAll(newBlock.getTransactions());
			return true;
		}
		return false;
	}
	
	/**
	 * Verify if the new block is valid
	 * 
	 * @param newBlock
	 * @param previousBlock
	 * @return
	 */
	public boolean isValidNewBlock(Block newBlock, Block previousBlock) {
		if (!previousBlock.getHash().equals(newBlock.getHashPrevious())) {
			System.out.println("The previous block hash verification of the new block failed");
			return false;
		} else {
			// Verify the correctness of the new block's hash value
			String hash = calculateHash(newBlock.getHashPrevious(), newBlock.getTransactions(), newBlock.getNonce());
			if (!hash.equals(newBlock.getHash())) {
				System.out.println("The new block's hash is invalid: " + hash + " " + newBlock.getHash());
				return false;
			}
			if (!isValidHash(newBlock.getHash())) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * Verify if the hash value meets the system requirements
	 * 
	 * @param hash
	 * @return
	 */
	public boolean isValidHash(String hash) {
		return hash.startsWith("0000");
	}
	
	/**
	 * Verify if the entire blockchain is valid
	 * @param chain
	 * @return
	 */
	public boolean isValidChain(List<Block> chain) {
		Block block = null;
		Block lastBlock = chain.get(0);
		int currentIndex = 1;
		while (currentIndex < chain.size()) {
			block = chain.get(currentIndex);

			if (!isValidNewBlock(block, lastBlock)) {
				return false;
			}

			lastBlock = block;
			currentIndex++;
		}
		return true;
	}

	/**
	 * Replace the local blockchain
	 * 
	 * @param newBlocks
	 */
	public void replaceChain(List<Block> newBlocks) {
		List<Block> localBlockChain = blockCache.getBlockChain();
		List<Transaction> localpackedTransactions = blockCache.getPackedTransactions();
		if (isValidChain(newBlocks) && newBlocks.size() > localBlockChain.size()) {
			localBlockChain = newBlocks;
			// Replace the collection of packaged saved business data
			localpackedTransactions.clear();
			localBlockChain.forEach(block -> {
				localpackedTransactions.addAll(block.getTransactions());
			});
			blockCache.setBlockChain(localBlockChain);
			blockCache.setPackedTransactions(localpackedTransactions);
			System.out.println("The replaced local node blockchain: "+JSON.toJSONString(blockCache.getBlockChain()));
		} else {
			System.out.println("The received blockchain is invalid");
		}
	}

	/**
	 * Calculate the hash of a block
	 * 
	 * @param previousHash
	 * @param currentTransactions
	 * @param nonce
	 * @return
	 */
	public String calculateHash(String previousHash, List<Transaction> currentTransactions, int nonce) {
		return sha256(previousHash + JSON.toJSONString(currentTransactions) + nonce);
	}


	/**
	 * Returns the SHA-256 hash of the input string in hexadecimal format.
	 *
	 * @param input The input string to hash
	 * @return The hashed string in hexadecimal format
	 * @throws RuntimeException if the hashing algorithm is not available or if the input string is null
	 */
	public static String sha256(String input) {
		if (input == null) {
			throw new RuntimeException("Input string cannot be null");
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			return bytesToHex(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorithm not available", e);
		}
	}

	/**
	 * Converts a byte array to a hexadecimal string.
	 *
	 * @param bytes The byte array to convert
	 * @return The hexadecimal string representation of the byte array
	 */
	private static String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

}
