package com.cs6650.blockchain.p2p;

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * P2P server
 */
@Component
public class P2PServer {

	@Autowired
    P2PService p2pService;

	public void initP2PServer(int port) {
		WebSocketServer socketServer = new WebSocketServer(new InetSocketAddress(port)) {

			/**
			 * Triggered after the connection is established
			 */
			@Override
			public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
				p2pService.getSockets().add(webSocket);
			}

			/**
			 * Triggered after the connection is closed
			 */
			@Override
			public void onClose(WebSocket webSocket, int i, String s, boolean b) {
				p2pService.getSockets().remove(webSocket);
				System.out.println("connection closed to address: " + webSocket.getRemoteSocketAddress());
			}

			/**
			 * Triggered when a message is received from the client
			 */
			@Override
			public void onMessage(WebSocket webSocket, String msg) {
				// As a server, handle the business logic
				p2pService.handleMessage(webSocket, msg, p2pService.getSockets());
			}

			/**
			 * Triggered when an error occurs
			 */
			@Override
			public void onError(WebSocket webSocket, Exception e) {
				p2pService.getSockets().remove(webSocket);
				System.out.println("connection failed to address: " + webSocket.getRemoteSocketAddress());
			}

			@Override
			public void onStart() {

			}

		};
		socketServer.start();
		System.out.println("listening websocket p2p port on: " + port);
	}
}
