package io.study.messagedelayusingheader.config.chatting;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class UserMessageConsumerService {

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

	@RabbitListener(queues = "MESSAGE_DELAY_PUSH_QUEUE", messageConverter = "mqMessageConverter")
	public void receiveMessage(final UserMessageDto message){
		final String currentDateTime = OffsetDateTime.now().format(formatter);
		System.out.println(currentDateTime + " [메시지 데이터 수신] message = " + message.toString());
	}
}
