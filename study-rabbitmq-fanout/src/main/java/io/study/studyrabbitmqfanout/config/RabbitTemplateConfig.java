package io.study.studyrabbitmqfanout.config;

import org.springframework.amqp.rabbit.batch.SimpleBatchingStrategy;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

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

	@Bean(name = "batchingRabbitTemplate")
	public BatchingRabbitTemplate batchingRabbitTemplate(
		ConnectionFactory connectionFactory,
		@Qualifier("mqMessageConverter") MessageConverter messageConverter
	){
		int batchSize = 5000; 		// 5천건을 batch size 로 지정
		int bufferLimit = 7500000; 	// 메시지 한건이 150Byte 일 경우 150 * 5000 = 750000 Byte, 750KByte
		long timeout = 30000; 		// 타임아웃은 30초
		SimpleBatchingStrategy strategy = new SimpleBatchingStrategy(batchSize, bufferLimit, timeout);
		TaskScheduler taskScheduler = new ConcurrentTaskScheduler();

		final BatchingRabbitTemplate batchingRabbitTemplate = new BatchingRabbitTemplate(connectionFactory, strategy, taskScheduler);
		return batchingRabbitTemplate;
	}

}
