package io.study.messagedelayusingheader.config.chatting;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class UserMessageProducerRunner implements CommandLineRunner {

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	private final RabbitTemplate rabbitTemplate;
	private final FanoutExchange fanoutExchange;

	public UserMessageProducerRunner(
		@Qualifier("producerRabbitTemplate") RabbitTemplate rabbitTemplate,
		@Qualifier("messageDelayPushExchange") FanoutExchange fanoutExchange
	){
		this.rabbitTemplate = rabbitTemplate;
		this.fanoutExchange = fanoutExchange;
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println(OffsetDateTime.now().format(formatter));
		UserMessageDto message = createMessageDto();
		// sendMessageByBuilder(createMessageWithBuilder(message));
		sendMessageByPostProcessor(message);
	}

	public void sendMessageByBuilder(Message message){
		rabbitTemplate.convertAndSend(fanoutExchange.getName(), "", message);
	}

	public void sendMessageByPostProcessor(UserMessageDto messageDto){
		rabbitTemplate.convertAndSend(fanoutExchange.getName(), "", messageDto, new MessagePostProcessor() {
			@Override
			public Message postProcessMessage(Message message) throws AmqpException {
				message.getMessageProperties().setDelay(1000);
				return message;
			}
		});
	}

	public Message createMessageWithBuilder(UserMessageDto messageDto){
		MessageProperties properties = new MessageProperties();
		properties.setDelay(1000);
		return MessageBuilder
			.withBody(messageDto.toString().getBytes())
			.andProperties(properties)
			.build();
	}

	public UserMessageDto createMessageDto(){
		String currentDateTime = OffsetDateTime.now().format(formatter);
		StringBuffer buffer = new StringBuffer();
		buffer
			.append("[")
			.append(currentDateTime)
			.append("] ")
			.append("메시지 생성");

		return UserMessageDto.builder()
			.content(buffer.toString())
			.build();
	}
}
