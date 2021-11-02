package io.study.stompfanout.config.rabbit;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.study.stompfanout.constant.Cafe;

@Configuration
public class CafeTopicQueueConfig {

	@Bean(name = "cafeQueue")
	public Queue sampleQueue(){
		Map arguments = new HashMap();
		arguments.put("x-message-ttl", 1000*2);
		return new Queue(Cafe.RabbitEnvironment.QUEUE_NAME, false, false, false, arguments);
	}

	@Bean(name = "cafeTopicExchange")
	public TopicExchange cafeTopicExchange(){
		return ExchangeBuilder
			.topicExchange(Cafe.RabbitEnvironment.TOPIC_EXCHANGE_NAME)
			.build();
	}

	@Bean(name = "allMenuBindingWithFullDepth")
	public Binding allMenuBindingWithFullDepth(
		@Qualifier("cafeTopicExchange") TopicExchange cafeTopicExchange,
		@Qualifier("cafeQueue") Queue cafeQueue
	){
		return BindingBuilder
			.bind(cafeQueue)
			.to(cafeTopicExchange)
			.with(Cafe.RabbitEnvironment.ROUTING_ALL_FULL_DEPTH);
	}
}
