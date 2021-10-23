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
		Map arguments = new HashMap();
		arguments.put("x-message-ttl", 1000);  // 1초 전의 데이터까지는 새로 접속한 사람도 모두 받는다.
		final Queue pricePushQueue = new Queue("messagePushQueue", false, false, false, arguments);
		return pricePushQueue;
	}

	@Bean(name = "messagePushDelayedQueue")
	public Queue messagePushDelayedQueue(){
		Map arguments = new HashMap();
		arguments.put("x-message-ttl", 1000);	// 1초 전의 데이터까지는 새로 접속한 사람도 모두 받는다.
		final Queue pricePushDelayedQueue = new Queue("messagePushDelayedQueue", false, false, false, arguments);
		return pricePushDelayedQueue;
	}

	@Bean(name = "messagePushExchange")
	public FanoutExchange messagePushExchange(){
		return ExchangeBuilder
			.fanoutExchange("MESSAGE_PUSH_EXCHANGE")
			.build();
	}

	@Bean(name = "messagePushDelayedExchange")
	public FanoutExchange messagePushDelayedExchange(){
		return ExchangeBuilder
			.fanoutExchange("MESSAGE_PUSH_DELAYED_EXCHANGE")
			.delayed()
			.build();
	}

	@Bean(name = "messagePushBinding")
	public Binding messagePushBinding(
		@Qualifier("messagePushExchange") FanoutExchange exchange,
		@Qualifier("messagePushQueue") Queue queue
	){
		return BindingBuilder.bind(queue).to(exchange);
	}

	@Bean(name = "messagePushDelayedBinding")
	public Binding messagePushDelayedBinding(
		@Qualifier("messagePushDelayedExchange") FanoutExchange exchange,
		@Qualifier("messagePushDelayedQueue") Queue queue
	){
		return BindingBuilder.bind(queue).to(exchange);
	}
}
