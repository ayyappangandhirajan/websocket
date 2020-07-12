package com.acme.bookshop.cloudapi.websocket.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class TroubleTicketHandler extends TextWebSocketHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(TroubleTicketHandler.class);
	private static Map<String, String> customers = new HashMap<>();
	private static Map<String, Conversation> sessions = new HashMap<>();
	
	static {
		customers.put("123", "John");
		customers.put("456", "Stephen");
		customers.put("789", "Hans");
	}
	
	private String[] queries = {
			"Please provide your product serial number", 
			"Please describe your complaint in few lines", 
			"Your complaint reference number is 7890. Thank you"
	};
	
	private List<String> queriesList = Arrays.asList(queries);
	
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message)
			throws InterruptedException, IOException {
		logger.info("Received request from {} {} {}", 
				session.getId(), 
				session.getRemoteAddress(),
				session.toString());
		logger.info("Request message: {}", message.getPayload());
		String payload = message.getPayload();
		JSONObject jsonObject = new JSONObject(payload);
		String msg = jsonObject.get("msg").toString();
		String responseMsg = process(session, msg);
		//session.sendMessage(new TextMessage("Hi " + jsonObject.get("msg") + " how may we help you?"));
		session.sendMessage(new TextMessage(responseMsg));
	}
	
	
	
	private String process(WebSocketSession session, String msg) {
		logger.info("Processing request for session: {}", session.getId());
		if(sessions.get(session.getId()) == null){
			return "Invalid session. Please create new session";
		} 
		
		Conversation conv = sessions.get(session.getId());
		if(conv.state == 0) {
			logger.info("State: {}, msg: {}", conv.state, msg);

			String customerId = msg;
			if(!isValidUser(customerId)) {
				return "Sorry, we could not identify you in our database. Please provide correct customer id (123 | 456 | 789) or alternatively use our phone banking service";
			}
			
			logger.info("Customer id is authenticated");
			conv.state = 1;
			return "Please provide your product serial number";
		} else if(conv.state == 1) {
			String psn = msg;
			if(!isValidProductSerialNumber(psn)) {
				return "Sorry, we could not identify your product in our database. Please provide correct product serial number (1234567890) or alternatively use our phone banking service";
			}
			
			logger.info("PSN is authenticated");
			conv.state = 2;
			return "Please describe your complaint in few lines";
		} else if(conv.state == 2) {
			String complaint = msg;
			if(!isValidProductComplaint(complaint)) {
				return "Sorry, your complaint description is too small or too long (should be between 50 to 200 characters). Please use our phone banking service";
			}
			
			logger.info("Complaint is taken ");
			conv.state = 3;
			return "Your complaint reference number is 7890. Thank you";
		}
		
		return "For new complaint, please disconnect and connect again. Alternatively, please use our phone banking service";
	}



	private boolean isValidProductComplaint(String complaint) {
		return complaint.length() > 50 && complaint.length() < 200;
	}



	private boolean isValidProductSerialNumber(String psn) {
		return psn.equals("1234567890");
	}



	private boolean isValidUser(String customerId) {
		logger.info("is valid customer {}", customers.get(customerId) != null);
		return customers.get(customerId) != null;
	}



	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		logger.info("Connection established with {} {} {}",session.getId(), 
				session.getRemoteAddress(),
				session.toString());
		sessions.put(session.getId(), new Conversation());
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		logger.info("Transport error with {} {} {}",session.getId(), 
				session.getRemoteAddress(),
				session.toString(), exception);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		logger.info("Connection closed from {} {} {}",session.getId(), 
				session.getRemoteAddress(),
				status);
		sessions.remove(session.getId());
	}

}