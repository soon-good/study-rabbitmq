package io.study.studyrabbitmqfanout.config.queue.test;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Profile("test-rabbitmq-postgresql")
@Service
public class TestMessagePushProducerService {
	private final RabbitTemplate rabbitTemplate;
	private final FanoutExchange fanoutExchange;
	private final BatchingRabbitTemplate batchingRabbitTemplate;

	public TestMessagePushProducerService(
		@Qualifier("producerRabbitTemplate") final RabbitTemplate rabbitTemplate,
		@Qualifier("batchingRabbitTemplate") final BatchingRabbitTemplate batchingRabbitTemplate,
		@Qualifier("messagePushExchange") final FanoutExchange fanoutExchange
	){
		this.rabbitTemplate = rabbitTemplate;
		this.batchingRabbitTemplate = batchingRabbitTemplate;
		this.fanoutExchange = fanoutExchange;
	}

	@Scheduled(initialDelay = 1000, fixedRate = 500)
	public void sendMessageFixedRate(){
		// List<TestMessageDto> list = TestMessageDto.selectSampleMessage(10);
		// for(TestMessageDto message : list){
		// 	System.out.println("[데이터 전송] " + message.getMessage());
		// 	rabbitTemplate.convertAndSend(fanoutExchange.getName(), "", message);
		// }
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
		TestMessageDto msg = TestMessageDto.builder()
			.message("메시지 " + OffsetDateTime.now().format(formatter))
			.build();
		System.out.println("[데이터 전송] " + msg.getMessage());
		rabbitTemplate.convertAndSend(fanoutExchange.getName(), "", msg);
	}

	// @Scheduled(initialDelay = 1000, fixedRate = 500)
	public void sendBulkMessage(){
		List<TestMessageDto> list = TestMessageDto.selectSampleMessage(10);
		for(TestMessageDto message : list){
			batchingRabbitTemplate.convertAndSend(fanoutExchange.getName(), "", message);
			// 또는 아래와 같이 구현해줘도 된다.
			// batchingRabbitTemplate.send(fanoutExchange.getName(), "", _message); // message 를 Message 객체내에 감싸서 만든 객체를 넣어줘야 한다.
		}
	}

}
