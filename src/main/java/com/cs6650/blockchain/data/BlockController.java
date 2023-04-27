package com.cs6650.blockchain.data;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.cs6650.blockchain.pow.PowService;

@Controller
public class BlockController {

	@Resource
	BlockService blockService;
	
	@Resource
	PowService powService;
	
	@Autowired
	BlockCache blockCache;
	
	/**
	 * View the blockchain data of the current node
	 * @return
	 */
	@GetMapping("/scan")
	@ResponseBody
	public String scanBlock() {
		return JSON.toJSONString(blockCache.getBlockChain());
	}
	
	/**
	 * View the blockchain data of the current node
	 * @return
	 */

	@GetMapping("/data")
	@ResponseBody
	public String scanData() {
		return JSON.toJSONString(blockCache.getPackedTransactions());
	}



	/**
	 * Create the genesis block
	 * @return
	 */
	@GetMapping("/create")
	@ResponseBody
	public String createFirstBlock() {
		blockService.createGenesisBlock();
		return JSON.toJSONString(blockCache.getBlockChain());
	}
	
	/**
	 * Proof of Work (PoW)
	 * Mine a new block
	 */
	@GetMapping("/mine")
	@ResponseBody
	public String createNewBlock() {
		powService.mine();
		return JSON.toJSONString(blockCache.getBlockChain());
	}
}
