package io.study.studyrabbitmqfanout.config.queue.test;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("test-rabbitmq-postgresql")
@Service
public class TestMessagePushConsumerService {

	@RabbitListener(queues = "messagePushQueue", messageConverter = "mqMessageConverter")
	public void sysoutListen(final TestMessageDto message){
		System.out.println("[메시지 데이터 수신] message = " + message.toString());
	}
}
