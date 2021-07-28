package io.study.studyrabbitmqfanout.config.queue.test;

import java.util.List;

import org.springframework.amqp.core.FanoutExchange;
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

	public TestMessagePushProducerService(
		@Qualifier("producerRabbitTemplate") final RabbitTemplate rabbitTemplate,
		@Qualifier("messagePushExchange") final FanoutExchange fanoutExchange
	){
		this.rabbitTemplate = rabbitTemplate;
		this.fanoutExchange = fanoutExchange;
	}

	@Scheduled(initialDelay = 1000, fixedRate = 500)
	public void sendMessageFixedRate(){
		List<TestMessageDto> list = TestMessageDto.selectSampleMessage(10);
		for(TestMessageDto message : list){
			System.out.println("[데이터 전송] " + message.getMessage());
			rabbitTemplate.convertAndSend(fanoutExchange.getName(), "", message);
		}
	}

}
