package com.cs6650.blockchain.p2p;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * P2P client
 */
@Component
public class P2PClient {
	
	@Autowired
	P2PService p2pService;

	public void connectToPeer(String addr) {
		try {
			final WebSocketClient socketClient = new WebSocketClient(new URI(addr)) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					// Client sends request, queries the latest block
					p2pService.write(this, p2pService.queryLatestBlockMsg());
					p2pService.getSockets().add(this);
				}

				/**
				 * Triggered when a message is received
				 * @param msg
				 */
				@Override
				public void onMessage(String msg) {
					p2pService.handleMessage(this, msg, p2pService.getSockets());
				}

				@Override
				public void onClose(int i, String msg, boolean b) {
					p2pService.getSockets().remove(this);
					System.out.println("connection closed with address: "+addr);
				}

				@Override
				public void onError(Exception e) {
					p2pService.getSockets().remove(this);
					System.out.println("connection failed with address: "+addr);
				}
			};
			socketClient.connect();
		} catch (URISyntaxException e) {
			System.out.println("P2P connect error with address: " +addr+", error message: "+ e.getMessage());
		}
	}

}
