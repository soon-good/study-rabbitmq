package io.study.studybatchingrabbittemplate.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqMessageConverterConfig {

	@Bean(name = "mqMessageConverter")
	public MessageConverter mqMessageConverter(){
		return new Jackson2JsonMessageConverter();
	}
}
