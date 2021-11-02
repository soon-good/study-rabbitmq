package io.study.stompfanout.cafe;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.study.stompfanout.constant.Cafe;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EnableScheduling
@Component
public class MenuService {

	private final RabbitTemplate rabbitTemplate;

	@Scheduled(initialDelay = 10, fixedDelay = 1000)
	public void producingAmericano(){
		MenuItem americano = MenuItem.builder().itemName(Cafe.MenuItem.AMERICANO).price(3000).build();

		// rabbitTemplate.convertAndSend(
		// 	Cafe.RabbitEnvironment.TOPIC_EXCHANGE_NAME,
		// 	Cafe.RabbitEnvironment.ROUTING_AMERICANO,
		// 	americano
		// );
	}
}
