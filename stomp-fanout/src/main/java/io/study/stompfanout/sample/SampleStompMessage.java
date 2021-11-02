package io.study.stompfanout.sample;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SampleStompMessage {

	private String type;
	private String username;
	private String content;

	@Builder
	public SampleStompMessage(String type, String username, String content){
		this.type = type;
		this.username = username;
		this.content = content;
	}
}
