package io.study.messagedelayusingheader.config.queue;

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

@Configuration
public class MessageDelayPushQueueConfig {

	@Bean(name = "messageDelayPushQueue")
	public Queue messageDelayPushQueue(){
		Map args = new HashMap();
		args.put("x-message-ttl", 1000);
		final Queue messageDelayQueue = new Queue("MESSAGE_DELAY_PUSH_QUEUE", false, true, true, args);
		return messageDelayQueue;
	}

	@Bean(name = "messageDelayPushExchange")
	public FanoutExchange messageDelayPushExchange(
	){
		return ExchangeBuilder.fanoutExchange("MESSAGE_DELAY_PUSH_EXCHANGE")
			.delayed()
			.build();
	}

	@Bean(name = "messageDelayPushBinding")
	public Binding messageDelayPushBinding(
		@Qualifier("messageDelayPushQueue") Queue queue,
		@Qualifier("messageDelayPushExchange") FanoutExchange exchange
	){
		return BindingBuilder.bind(queue).to(exchange);
	}
}
