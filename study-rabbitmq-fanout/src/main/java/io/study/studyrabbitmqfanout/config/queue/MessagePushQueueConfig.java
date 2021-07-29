package io.study.studyrabbitmqfanout.config.queue;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("test-rabbitmq-postgresql")
@Configuration
public class MessagePushQueueConfig {

	@Bean(name = "messagePushQueue")
	public Queue messagePushQueue(){
		final Queue pricePushQueue = new Queue("messagePushQueue");
		return pricePushQueue;
	}

	@Bean(name = "messagePushDelayedQueue")
	public Queue messagePushDelayedQueue(){
		Map arguments = new HashMap();
		arguments.put("x-dead-letter-exchange", "x2");
		arguments.put("x-message-ttl", 5000L);
		final Queue pricePushDelayedQueue = new Queue("messagePushDelayedQueue", false, false, false, arguments);
		return pricePushDelayedQueue;
	}

	@Bean(name = "messagePushExchange")
	public FanoutExchange messagePushExchange(){
		return ExchangeBuilder
			.fanoutExchange("MESSAGE_PUSH_EXCHANGE")
			.build();
	}

	@Bean(name = "messagePushBinding")
	public Binding messagePushBinding(
		@Qualifier("messagePushExchange") FanoutExchange exchange,
		@Qualifier("messagePushQueue") Queue queue
	){
		return BindingBuilder.bind(queue).to(exchange);
	}
}
