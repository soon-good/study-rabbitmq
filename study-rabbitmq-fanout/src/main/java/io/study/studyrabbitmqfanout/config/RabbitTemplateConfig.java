package io.study.studyrabbitmqfanout.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTemplateConfig {
	@Value("${spring.rabbitmq.host}")
	private String host;
	@Value("${spring.rabbitmq.port}")
	private int port;
	@Value("${spring.rabbitmq.username}")
	private String username;
	@Value("${spring.rabbitmq.password}")
	private String password;

	@Bean(name = "mqMessageConverter")
	public MessageConverter mqMessageConverter(){
		return new Jackson2JsonMessageConverter();
	}

	@Bean(name = "producerRabbitTemplate")
	public RabbitTemplate producerRabbitTemplate(
		ConnectionFactory connectionFactory,
		@Qualifier("mqMessageConverter") MessageConverter messageConverter
	){
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(messageConverter);
		return rabbitTemplate;
	}

	@Bean(name = "consumerRabbitTemplate")
	public RabbitTemplate consumerRabbitTemplate(
		// @Qualifier("rabbitMqConnectionFactory") ConnectionFactory connectionFactory,
		ConnectionFactory connectionFactory,
		@Qualifier("mqMessageConverter") MessageConverter messageConverter
	){
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(messageConverter);
		return rabbitTemplate;
	}

}
