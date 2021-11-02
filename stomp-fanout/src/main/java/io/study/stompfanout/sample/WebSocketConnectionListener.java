package io.study.stompfanout.sample;

import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketConnectionListener {

	private final SimpMessageSendingOperations messagingTemplate;

	public WebSocketConnectionListener(SimpMessageSendingOperations messagingTemplate){
		this.messagingTemplate = messagingTemplate;
	}

	@EventListener
	public void sessionConnected(SessionConnectedEvent connectedEvent){
		log.info(String.format("접속세션 생성"));
	}

	@EventListener
	public void sessionDisconnected(SessionDisconnectEvent disconnectEvent){
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(disconnectEvent.getMessage());

		// String username = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("username");
		// if(username != null){
		// 	SampleStompMessage message = SampleStompMessage.builder()
		// 		.type("Leave")
		// 		.username(username)
		// 		.build();
		//
		// 	messagingTemplate.convertAndSend("/topic/public", message);
		// }
	}
}
