# 메시지 Batching 발송 - BatchingRabbitTemplate

메시지 배치처리는 브로커인 레빗엠큐 측에 설정하는 것이 아니라 생산자 측에서 메시지를 묶어서 브로커인 레빗엠큐에게 전달해주면 된다. 스프링을 사용하는 경우 RabbitMQ 브로커로의 생산자/소비자 코드를 작성할 때 Spring 에서 AMQP 프로토콜을 할 수 있도록 제공해주는 라이브러리인 Spring AMQP 라이브러리를 사용한다. <br>

이 Spring AMQP 라이브러리에서는 Spring AMQP 1.4.2 에서부터 BatchingRabbitTemplate 이라는 클래스를 제공해주는데, 이 BatchingRabbitTemplate 을 활용해서 단건의 메시지를 batchSize 단위로 묶어서 묶음으로 발송할 수 있다.<br>

BatchingRabbitTemplate 은 RabbitTemplate 을 확장한(상속받은) 클래스이다.<br>

send() 또는 convertAndSend () 메서드 내에 BatchingStrategy 타입의 인스턴스를 메서드의 인자로 전달해주는 방식으로 Batching 방식을 결정지을 수 있다. (타입이나 이런 것은 모두 공식 문서 [Spring AMQP - Batching](https://docs.spring.io/spring-amqp/docs/current/reference/html/#template-batching) 를 참고) <br>

<br>

아래에 정리한 예제들의 전체 코드는 [github.com/gosgjung/study-rabbitmq](https://github.com/gosgjung/study-rabbitmq/tree/develop/message-delay/message-delay-1-using-header) 에 정리해두었다.

<br>

## 참고자료

- [docs.spring.io - Spring AMQP](https://docs.spring.io/spring-amqp/docs/current/reference/html)
- [Spring AMQP - Batching](https://docs.spring.io/spring-amqp/docs/current/reference/html/#template-batching)
  - 생산자 측에서의 배치 처리이다.
- [Spring AMQP - @RabbitListener with Batching](https://docs.spring.io/spring-amqp/docs/current/reference/html/#receiving-batch)
  - 소비자 측에서의 배치 처리.
  - 리슨하는 쪽에서 배치방식으로 리슨하는 방식이다.
- [ProgrammerSought - Spring Boot Message Queue RabbitMQ Getting Started Series Tutorial](https://www.programmersought.com/article/41295475933/)
  - BatchingRabbitTemplate 을 사용하는 방식에 대해서도 설명해주고 있다.

<br>

## 메시지 큐 Configuration

브로커에 접속하기 위해 정의한 설정이다. 기본적인 RabbitMQ 설정 코드에서 크게 달라지는 내용은 없다. <br>

```java
@Configuration
public class MessagePushQueueConfig {

	@Bean(name = "messagePushQueue")
	public Queue messagePushQueue(){
		Map arguments = new HashMap();
		arguments.put("x-message-ttl", 1000);  // 1초 전의 데이터까지는 새로 접속한 사람도 모두 받는다.
		final Queue pricePushQueue = new Queue("messagePushQueue", false, false, true, arguments);
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
```

<br>

## 메시지 컨버터 Configuration

아래는 메시지 Converter 관련 설정이다. 메시지를 받을때 직렬화를 해서 Dto로 변환할수 있도록 도와주는 `Jackson2JsonMessageConverter` 인스턴스를 스프링의 런타임 컨텍스트에 빈으로 추가하는 설정코드이다.

```java
@Configuration
public class MqMessageConverterConfig {

	@Bean(name = "mqMessageConverter")
	public MessageConverter mqMessageConverter(){
		return new Jackson2JsonMessageConverter();
	}
}
```

<br>

그리고 메시지 데이터를 보내는 단위인 Dto는 아래와 같이 구성했다. 

```java
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@ToString
public class UserMessageDto implements Serializable {
	private String content;
}
```

<br>

## 메시지 발송 Batch Configuration

**BatchingRabbitTemplateConfig.java**<br>

배치방식으로 메시지를 전송하기 위한 설정이다. Spring AMQP에서는 RabbitTemplate 클래스의 기능을 확장한 BatchingRabbitTemplate 클래스를 제공해준다. 이 BatchingRabbitTemplate 클래스의 인스턴스를 생성하기 위해서는 생성자를 직접 호출해서 생성해야 한다. 따로 빌더 패턴으로 제공해주는 것이 없다는 점은 불편한 점이기는 하다.<br>

[BatchingRabbitTemplate](https://docs.spring.io/spring-amqp/docs/current/api/org/springframework/amqp/rabbit/core/BatchingRabbitTemplate.html) 클래스의 생성자는 두가지 버전이 있는데, 아래 예제에서 사용한 것은 ConnectionFactory, BatchingStrategy, TaskScheduler 를 미리 세팅해서 넘겨받아 BatchingRabbitTemplate 의 인스턴스를 생성하는 예제이다.<br>

배치를 어떻게 하는지에 대한 동작은 BatchingStrategy 인스턴스를 생성하여 정의한다. BatchingStrategy 는 인터페이스이기에  BatchingStrategy 를 implements 하는 클래스를 사용해야 하는데, 아래 예제에서는 SimpleBatchingStrategy 를 인스턴스로 생성해서 사용했다.<br>

```java
@Configuration
public class BatchingRabbitTemplateConfig {

	@Bean(name = "messageBatchingRabbitTemplate")
	public BatchingRabbitTemplate batchingRabbitTemplate(
		ConnectionFactory connectionFactory,
		@Qualifier("mqMessageConverter") MessageConverter messageConverter
	){
		int batchSize = 10; 				// 10 개의 메시지를 batch size 로 지정
		int bufferLimit = 7500000; 	// 배치로 묶은 메시지의 크기가 75KByte 를 넘으면 강제 전송
		long timeout = 30000; 			// 타임아웃은 30초
		SimpleBatchingStrategy strategy = new SimpleBatchingStrategy(batchSize, bufferLimit, timeout);
		TaskScheduler taskScheduler = new ConcurrentTaskScheduler();

		final BatchingRabbitTemplate batchingRabbitTemplate = new BatchingRabbitTemplate(connectionFactory, strategy, taskScheduler);
		batchingRabbitTemplate.setMessageConverter(messageConverter);
		return batchingRabbitTemplate;
	}
}
```

- batchSize
  - 메시지를 보낼때 10개의 메시지를 모아서 하나의 묶음으로 전송하겠다는 의미이다.
- bufferLimit
  - 배치처리시 배치 묶음의 사이즈의 한계치이다.
  - bufferLimit 을 아래 코드에서는 7500000 으로 지정해주었는데, 배치로 묶은 메시지의 크기가 75KByte를 넘으면 강제전송하겠다는 의미이다.
- timeout
  - 배치의 타임아웃이다.
  - 타임아웃은 아래 코드에서는 30초로 지정해주었다.

batchSize, bufferLimit, timeout 에 대한 자세한 내용은 [docs.spring.io/spring-amqp](https://docs.spring.io/spring-amqp/docs/current/api/org/springframework/amqp/rabbit/batch/SimpleBatchingStrategy.html#SimpleBatchingStrategy-int-int-long-) 를 참고.

<br>

## 메시지 발송 코드

샘플 메시지를 발송을 1초에 한 번씩 하기 위해 스케쥴러가 동작할 수 있도록 `@EnableScheduling` 을 추가한 설정을 추가해주었다.

```java
@EnableScheduling
@Configuration
public class SchedulingConfig {
}
```

<br>

메시지를 발송하는 서비스 코드는 아래와 같이 추가해주었다.

```java
@Service
public class UserMessageProducerService {
	private final DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

	private final BatchingRabbitTemplate batchingRabbitTemplate;
	private final FanoutExchange fanoutExchange;

	public UserMessageProducerService(
		@Qualifier("messageBatchingRabbitTemplate") BatchingRabbitTemplate batchingRabbitTemplate,
		@Qualifier("messagePushExchange") FanoutExchange fanoutExchange
	){
		this.batchingRabbitTemplate = batchingRabbitTemplate;
		this.fanoutExchange = fanoutExchange;
	}

	@Scheduled(initialDelay = 0, fixedDelay = 1000L)
	public void sendUserMessageBatching(){
		StringBuffer buffer1 = new StringBuffer();
		buffer1
			.append("[")
			.append(OffsetDateTime.now().format(pattern))
			.append("] ")
			.append("메시지 생성");

		final UserMessageDto messageDto = UserMessageDto.builder()
			.content(buffer1.toString())
			.build();

		batchingRabbitTemplate.convertAndSend(fanoutExchange.getName(), "", messageDto);

		StringBuffer buffer2 = new StringBuffer();
		buffer2
			.append("[")
			.append(OffsetDateTime.now().format(pattern))
			.append("] ")
			.append("데이터 전송 완료");
		System.out.println(buffer2.toString());
	}
}
```

<br>

## @RabbitListener (1) - 메시지 단건 리스닝 

메시지 발송코드로 생산자 코드를 작성했는데, 메시지 발송시에 Batch 처리를 해서 메시지를 묶음으로 전송하도록 했었다. 소비자 측인 @RabbitListener 측의 코드에서 별다른 설정 없이 일반적인 메시지 리슨 로직과 같게 작성한 코드는 아래와 같다. 

```java
package io.study.studybatchingrabbittemplate.chatting.consumer;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import io.study.studybatchingrabbittemplate.chatting.UserMessageDto;

@Service
public class UserMessageConsumerService {
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

	// 단건으로 배치 메시지 받기
	@RabbitListener(queues = "messagePushQueue", messageConverter = "mqMessageConverter")
	public void getUserMessage(final UserMessageDto message){
		final String currentDateTime = OffsetDateTime.now().format(formatter);
		System.out.println(currentDateTime + " [메시지 데이터 수신] message = " + message.toString());
	}
}

```

<br>

이 코드를 실행하면 아래와 같은 결과가 나타난다.

```plain
[2021/08/01 19:06:09] 데이터 전송 완료
[2021/08/01 19:06:10] 데이터 전송 완료
[2021/08/01 19:06:11] 데이터 전송 완료
[2021/08/01 19:06:12] 데이터 전송 완료
[2021/08/01 19:06:13] 데이터 전송 완료
[2021/08/01 19:06:14] 데이터 전송 완료
[2021/08/01 19:06:15] 데이터 전송 완료
[2021/08/01 19:06:16] 데이터 전송 완료
[2021/08/01 19:06:17] 데이터 전송 완료
[2021/08/01 19:06:18] 데이터 전송 완료
2021/08/01 19:06:18 [메시지 데이터 수신] message = UserMessageDto(content=[2021/08/01 19:06:09] 메시지 생성)
2021/08/01 19:06:18 [메시지 데이터 수신] message = UserMessageDto(content=[2021/08/01 19:06:10] 메시지 생성)
2021/08/01 19:06:18 [메시지 데이터 수신] message = UserMessageDto(content=[2021/08/01 19:06:11] 메시지 생성)
2021/08/01 19:06:18 [메시지 데이터 수신] message = UserMessageDto(content=[2021/08/01 19:06:12] 메시지 생성)
2021/08/01 19:06:18 [메시지 데이터 수신] message = UserMessageDto(content=[2021/08/01 19:06:13] 메시지 생성)
2021/08/01 19:06:18 [메시지 데이터 수신] message = UserMessageDto(content=[2021/08/01 19:06:14] 메시지 생성)
2021/08/01 19:06:18 [메시지 데이터 수신] message = UserMessageDto(content=[2021/08/01 19:06:15] 메시지 생성)
2021/08/01 19:06:18 [메시지 데이터 수신] message = UserMessageDto(content=[2021/08/01 19:06:16] 메시지 생성)
2021/08/01 19:06:18 [메시지 데이터 수신] message = UserMessageDto(content=[2021/08/01 19:06:17] 메시지 생성)
2021/08/01 19:06:18 [메시지 데이터 수신] message = UserMessageDto(content=[2021/08/01 19:06:18] 메시지 생성)
```

<br>

우선 데이터 전송은 1초에 한번씩 주기적으로 전송하고 있다. 그리고 10개의 메시지 전송이 모두 끝나면 메시지를 수신하고 있다. 10개의 메시지를 모두 convertAndSend()했지만, 실제 소켓 메시지로 보내는 것은 10개의 메시지를 모두 convertAndSend() 한 이후에 메시지를 보내는 것임을 유추가능하다.<br>

데이터 수신 측에서는 단건으로 `UserMessageDto` 를 하나씩 받고 있음을 알 수 있다. 10개의 메시지를 일일이 하나씩 받고 있다. 만약 이렇게 전달된 10개의 데이터를 Database에 insert 하는 경우를 예로 들어보자. 트래픽이 많이 발생하는 경우 배치 메시지는 더 자주 발생할 것이다. 그리고 이것으로 인해 DB에 접근하는 ConnectionPool 도 Overflow 될때도 있지 않을까? 하는 생각도 해볼 수 있을 것 같다.<br>



## @RabbitListener (2) - 메시지 Batch 리스닝

이번에는 소비자 측에서도 10개의 메시지가 모두 모아서 받는 로직을 작성해보려 한다. 이것을 공식문서 [Spring AMQP - @RabbitListener with Batching](https://docs.spring.io/spring-amqp/docs/current/reference/html/#receiving-batch) 에서는 @RabbitListener With Batching 으로 소개하고 있다. 메시지의 리슨 역시 배치방식으로 받는 것을 의미한다.<br>

```java
@Service
public class UserMessageConsumerService {
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
  
  // ... 

	// 리스트로 배치 메시지 받기
	@RabbitListener(queues = "messagePushQueue", messageConverter = "mqMessageConverter", containerFactory = "messageBatchingListenerFactory")
	public void getUserMessageList(final List<UserMessageDto> list){
		final String currentDateTime = OffsetDateTime.now().format(formatter);
		System.out.println(currentDateTime + " [리스트 메시지 데이터 수신] >>> ");
		System.out.println(list);
	}
}

```

<br>

위의 코드와 같이 `@RabbitListener` 에 정의한 것처럼  `List<UserMessageDto>` 타입의 데이터를 받기 위해서는 아래와 같이 별도의 설정이 필요하다. <br>

아래 코드는 위 코드의 @RabbitListener 내의 containerFactory 필드에 지정해준 빈 인스턴스의 이름인  `messageBatchingListenerFactory` 에 대한 설정이다.<br>

자세한 내용은 [Spring AMQP - @RabbitListener with Batching](https://docs.spring.io/spring-amqp/docs/current/reference/html/#receiving-batch) 을 참고해서 작성했다. 아래 내용은 정말 단순한 설정이기에 설명은 생략한다. 더 자세하게 설정하면 세부적으로 설정해야될 게 굉장히 많다. 따라서 리스너측의 Batch 리스닝에 대한 설정은 문서를 따로 만들어 정리할 예정이다.<br>

```java
@Configuration
public class BatchingRabbitTemplateConfig {
  
  // ...

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

```

이제 설정을 마무리했다.<br>

지금까지 작성한 코드를 실행하면 아래와 같은 결과가 나타난다. 생산자 측에서 보낸 데이터를 `@RabbitListener` 에서 한번에 `List<UserMessageDto>` 를 받았다.

```plain
[2021/08/01 19:17:55] 데이터 전송 완료
[2021/08/01 19:17:56] 데이터 전송 완료
[2021/08/01 19:17:57] 데이터 전송 완료
[2021/08/01 19:17:58] 데이터 전송 완료
[2021/08/01 19:17:59] 데이터 전송 완료
[2021/08/01 19:18:00] 데이터 전송 완료
[2021/08/01 19:18:01] 데이터 전송 완료
[2021/08/01 19:18:02] 데이터 전송 완료
[2021/08/01 19:18:03] 데이터 전송 완료
[2021/08/01 19:18:04] 데이터 전송 완료
2021/08/01 19:18:04 [리스트 메시지 데이터 수신] >>> 
[UserMessageDto(content=[2021/08/01 19:17:55] 메시지 생성), UserMessageDto(content=[2021/08/01 19:17:56] 메시지 생성), UserMessageDto(content=[2021/08/01 19:17:57] 메시지 생성), UserMessageDto(content=[2021/08/01 19:17:58] 메시지 생성), UserMessageDto(content=[2021/08/01 19:17:59] 메시지 생성), UserMessageDto(content=[2021/08/01 19:18:00] 메시지 생성), UserMessageDto(content=[2021/08/01 19:18:01] 메시지 생성), UserMessageDto(content=[2021/08/01 19:18:02] 메시지 생성), UserMessageDto(content=[2021/08/01 19:18:03] 메시지 생성), UserMessageDto(content=[2021/08/01 19:18:04] 메시지 생성)]
```

<br>

리스트로 전달받은 데이터를 보기 쉽게 나열해보면 아래와 같은 모양이다. 

```plain
[
	UserMessageDto(content=[2021/08/01 19:17:55] 메시지 생성), 
	UserMessageDto(content=[2021/08/01 19:17:56] 메시지 생성), 
	UserMessageDto(content=[2021/08/01 19:17:57] 메시지 생성), 
	UserMessageDto(content=[2021/08/01 19:17:58] 메시지 생성), 
	UserMessageDto(content=[2021/08/01 19:17:59] 메시지 생성), 
	UserMessageDto(content=[2021/08/01 19:18:00] 메시지 생성), 
	UserMessageDto(content=[2021/08/01 19:18:01] 메시지 생성), 
	UserMessageDto(content=[2021/08/01 19:18:02] 메시지 생성), 
	UserMessageDto(content=[2021/08/01 19:18:03] 메시지 생성), 
	UserMessageDto(content=[2021/08/01 19:18:04] 메시지 생성)
]
```

