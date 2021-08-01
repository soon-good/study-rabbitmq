package io.study.studybatchingrabbittemplate.chatting.producer;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.study.studybatchingrabbittemplate.chatting.UserMessageDto;

@Service
public class UserMessageProducerService {
	private final DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

	private final BatchingRabbitTemplate batchingRabbitTemplate;
	private final FanoutExchange fanoutExchange;

	public UserMessageProducerService(
		@Qualifier("messageBatchingRabbitTemplate") BatchingRabbitTemplate batchingRabbitTemplate,
		@Qualifier("messagePushExchange") FanoutExchange fanoutExchange
	){
		this.batchingRabbitTemplate = batchingRabbitTemplate;
		this.fanoutExchange = fanoutExchange;
	}

	@Scheduled(initialDelay = 0, fixedDelay = 1000L)
	public void sendUserMessageBatching(){
		StringBuffer buffer1 = new StringBuffer();
		buffer1
			.append("[")
			.append(OffsetDateTime.now().format(pattern))
			.append("] ")
			.append("메시지 생성");

		final UserMessageDto messageDto = UserMessageDto.builder()
			.content(buffer1.toString())
			.build();

		batchingRabbitTemplate.convertAndSend(fanoutExchange.getName(), "", messageDto);

		StringBuffer buffer2 = new StringBuffer();
		buffer2
			.append("[")
			.append(OffsetDateTime.now().format(pattern))
			.append("] ")
			.append("데이터 전송 완료");
		System.out.println(buffer2.toString());
	}
}
