package io.study.studyrabbitmqfanout.config.queue;

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
