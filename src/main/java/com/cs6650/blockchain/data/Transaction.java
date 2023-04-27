package com.cs6650.blockchain.data;

import java.io.Serializable;

/**
 * Business data model
 */
public class Transaction implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
	 * Unique identifier
	 */
	private String id;
	/**
	 * Business data
	 */
	private String businessInfo;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBusinessInfo() {
		return businessInfo;
	}
	public void setBusinessInfo(String businessInfo) {
		this.businessInfo = businessInfo;
	}

}
