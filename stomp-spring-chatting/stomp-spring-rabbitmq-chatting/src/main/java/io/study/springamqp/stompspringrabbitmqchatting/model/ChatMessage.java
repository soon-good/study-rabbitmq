package io.study.springamqp.stompspringrabbitmqchatting.model;

import lombok.Data;

@Data
public class ChatMessage {
	private String type;
	private String content;
	private String sender;
}
