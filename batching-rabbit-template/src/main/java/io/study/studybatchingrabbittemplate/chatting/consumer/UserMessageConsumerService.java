package io.study.studybatchingrabbittemplate.chatting.consumer;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import io.study.studybatchingrabbittemplate.chatting.UserMessageDto;

@Service
public class UserMessageConsumerService {
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

	// 단건으로 배치 메시지 받기
	// @RabbitListener(queues = "messagePushQueue", messageConverter = "mqMessageConverter")
	// public void getUserMessage(final UserMessageDto message){
	// 	final String currentDateTime = OffsetDateTime.now().format(formatter);
	// 	System.out.println(currentDateTime + " [메시지 데이터 수신] message = " + message.toString());
	// }

	// 리스트로 배치 메시지 받기
	@RabbitListener(queues = "messagePushQueue", messageConverter = "mqMessageConverter", containerFactory = "messageBatchingListenerFactory")
	public void getUserMessageList(final List<UserMessageDto> list){
		final String currentDateTime = OffsetDateTime.now().format(formatter);
		System.out.println(currentDateTime + " [리스트 메시지 데이터 수신] >>> ");
		System.out.println(list);
	}
}
