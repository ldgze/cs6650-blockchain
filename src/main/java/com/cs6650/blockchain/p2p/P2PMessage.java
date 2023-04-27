package com.cs6650.blockchain.p2p;

import java.io.Serializable;

/**
 * P2P communication message
 */
public class P2PMessage implements Serializable {
	
	private static final long serialVersionUID = 1L;
	/**
	 * Message type
	 */
	private int type;
	/**
	 * Message content
	 */
	private String data;

	public P2PMessage() {
	}

	public P2PMessage(int type) {
		this.type = type;
	}

	public P2PMessage(int type, String data) {
		this.type = type;
		this.data = data;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
