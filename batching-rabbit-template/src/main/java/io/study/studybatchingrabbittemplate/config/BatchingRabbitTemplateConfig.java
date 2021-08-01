package io.study.studybatchingrabbittemplate.config;

import org.springframework.amqp.rabbit.batch.SimpleBatchingStrategy;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

@Configuration
public class BatchingRabbitTemplateConfig {

	@Bean(name = "messageBatchingRabbitTemplate")
	public BatchingRabbitTemplate batchingRabbitTemplate(
		ConnectionFactory connectionFactory,
		@Qualifier("mqMessageConverter") MessageConverter messageConverter
	){
		int batchSize = 10; 		// 10 개의 메시지를 batch size 로 지정
		int bufferLimit = 7500000; 	// 메시지 한건이 150Byte 일 경우 150 * 5000 = 750000 Byte, 750KByte
		long timeout = 30000; 		// 타임아웃은 30초
		SimpleBatchingStrategy strategy = new SimpleBatchingStrategy(batchSize, bufferLimit, timeout);
		TaskScheduler taskScheduler = new ConcurrentTaskScheduler();

		final BatchingRabbitTemplate batchingRabbitTemplate = new BatchingRabbitTemplate(connectionFactory, strategy, taskScheduler);
		batchingRabbitTemplate.setMessageConverter(messageConverter);
		return batchingRabbitTemplate;
	}

	@Bean(name = "messageBatchingListenerFactory")
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
		ConnectionFactory connectionFactory
	){
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setBatchListener(true);
		return factory;
	}
}
