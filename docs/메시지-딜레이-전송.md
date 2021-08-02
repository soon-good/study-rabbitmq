# 메시지 딜레이 전송

결론부터 말하면, 딜레이 기능은 Exchange Plugin 을 설치해야 한다. 만약 Amazon MQ 를 사용중인데, 관리형을 사용 중이라면 딜레이 기능은 사용 불가능하다.<br>

[docs.aws.amazon.com - Supported Plugins](https://docs.aws.amazon.com/amazon-mq/latest/migration-guide/amazon-mq-supported-plugins.html) 을 확인해보면, 지원되는 플러그인들 중에서는 Delayed Message Excahnge 플러그인은 지원하고 있지 않다.<br>

일단 오늘 까지는 작성한 소스코드와 출력결과만 일단 정리해놓을 예정이다. 일주일 뒤쯤 메시지 딜레이 관련해서 코드를 작성할 예정인데, 이 때부터 메시지 딜레이 테스트는 도커 컨테이너 기반으로 테스트해볼 예정이다.<br>

## 테스트 시나리오

- CommandLineRunner 로 애플리케이션 로딩 초기에 단 한번 메시지를 전송한다.
- 소비자측의 @RabbitListener 측에서는 메시지 리슨 시간과, 메시지에 포함된 메시지 생성시각을 비교해서 실제로 원하는 딜레이가 걸렸는지 확인한다.

<br>

## 참고자료

- [docs.spring.io - Delayed Message Exchange](https://docs.spring.io/spring-amqp/docs/current/reference/html/#delayed-message-exchange)
- [rabbitmq.com - Scheduling Messages with RabbitMQ](https://blog.rabbitmq.com/posts/2015/04/scheduling-messages-with-rabbitmq)
  - 레빗엠큐 확장 플러그인 소개 페이지
- [Rabbitmq-delayed-message-exchange 공식 페이지](https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/)
- [docs.aws.amazon.com - Supported Plugins](https://docs.aws.amazon.com/amazon-mq/latest/migration-guide/amazon-mq-supported-plugins.html)

<br>

## 메시지 큐 Configuration

제일 먼저 해야 하는 것은 메시지 브로커 내에서 익스체인지가 메시지 지연이 가능하도록 Enable 하는 것이다. 

예를 들면 아래와 같은 방식으로 Exchange에 delayed 되도록 Enable 해준다. 

```java
@Configuration
public class MessageDelayPushQueueConfig {
  // ...
	@Bean(name = "messageDelayPushExchange")
	public FanoutExchange messageDelayPushExchange(
	){
		return ExchangeBuilder.fanoutExchange("MESSAGE_DELAY_PUSH_EXCHANGE")
			.delayed()
			.build();
	}
  // ...
}
```

<br>

`ExchangeBuilder` 내의 `delayed()` 메서드는 `ExchangeBuilder` 클래스 내의 delayed 라는 멤버 필드의 값을 true 로 세팅해주는 역할을 한다. 이렇게 true 로 delayed를 세팅하고 나면 생산자에서 Exchange에 접촉할 때 메시지 헤더로 `x-delayed-message`을 전송한다.<br>

그런데, 이 `x-delayed-message`를 헤더로 해서 익스체인지에 메시지를 딜레이해주세요 하고 요청을 하려면, [rabbitmq-delayed-message-exchange](https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/) 라는 확장 플러그인을 설치해야 한다. 만약, 레빗엠큐 인스턴스에 확장 플러그인인 [rabbitmq-delayed-message-exchange](https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/)를 설치하지 않으면 `reply-code=503, reply-text=COMMAND_INVALID` 라는 에러문구를 접하게 된다.<br>



**메시지 큐 설정 전체 코드**<br>

```java
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
```

<br>

[rabbitmq-delayed-message-exchange](https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/) 를 설치하지 않았을 때 나타나는 에러문구는 아래와 같다.

```plain
Caused by: com.rabbitmq.client.ShutdownSignalException: connection error; protocol method: #method<connection.close>(reply-code=503, reply-text=COMMAND_INVALID - unknown exchange type 'x-delayed-message', class-id=40, method-id=10)
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

## RabbitTemplate 설정 

별다른 내용은 없다.

```java
@Configuration
public class RabbitTemplateConfig {
	@Bean(name = "producerRabbitTemplate")
	public RabbitTemplate producerRabbitTemplate(
		ConnectionFactory connectionFactory,
		@Qualifier("mqMessageConverter") MessageConverter messageConverter
	){
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(messageConverter);
		return rabbitTemplate;
	}
}
```

<br>

## 생산자 코드

생산자 코드에서 메시지 전달시에 메시지에 `메시지를 딜레이해주세요` 하는 헤더 값을 포함해서 익스체인지에 메시지를 전달해주는 과정이다. 테스트 결과를 명확하게 확인하기 위해 CommandLineRunner를 implements 한 클래스에 메시지 발송로직을 작성해 스프링 애플리케이션 로딩시 단 한번만 실행되도록 해주었다.<br>

메시지를 딜레이 하도록 메시지 헤더로 `x-delayed-type` 을 섞어서 보내는 방식의 핵심은  `org.springframework.amqp.core.Message` 객체의 properties 내의 delay 필드의 값을 지정해주는 것이다.<br>

`org.springframework.amqp.core.Message` 내의 properties를 얻어서 delay 필드를 세팅해주는 방식은 아래의 두가지 방식이 있다.

- MessageBuilder 로 메시지를 생성하는 방식
- MessagePostProcessor 를 이용해 메시지를 전송하는 방식
  - rabbitTemplate 의 마지막 인자로 `MessagePostProcessor` 인스턴스를 동적 생성하여 넘겨주는 방식
  - 이 방식은 람다표현식으로도 바꾸어 표현가능하다.

<br>

두가지 방식을 모두 정리해보면 아래와 같다.<br>

### MessageBuilder 로 메시지 생성하는 방식

핵심 코드는 아래와 같다. Message 객체를 인스턴스로 생성해서 rabbitTemplate의  convertAndSend() 메서드의 인자로 전달하여 메시지를 전송하고 있다.

```java
@Component
public class UserMessageProducerRunner implements CommandLineRunner{
  // ...
	public void sendMessageByBuilder(Message message){
		rabbitTemplate.convertAndSend(fanoutExchange.getName(), "", message);
	}

	public Message createMessageWithBuilder(UserMessageDto messageDto){
		MessageProperties properties = new MessageProperties();
		return MessageBuilder
			.withBody(messageDto.toString().getBytes())
			.andProperties(properties)
			.build();
	}
}
```

아래는 MessageBuilder 를 이용해 메시지를 전달하는 전체 예제 코드다.

```java
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
		sendMessageByBuilder(createMessageWithBuilder(message));
	}

	public void sendMessageByBuilder(Message message){
		rabbitTemplate.convertAndSend(fanoutExchange.getName(), "", message);
	}

	public Message createMessageWithBuilder(UserMessageDto messageDto){
		MessageProperties properties = new MessageProperties();
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
```

<br>

### MessagePostProcessor 를 이용해 메시지를 전송하는 방식

예를 들면 아래와 같은 코드로 메시지를 전송할 수 있다. MessagePostProcessor 를 인스턴스로 생성하는 구문인  `new MessagePostProcessor(){...}` 구문은 람다표현식으로도 바꾸어 표현가능하다. 이 방식 역시 바로 앞전에 살펴봤듯이 Message 객체의 properties 필드 내의 delay 필드를  `setDelay()` 메서드로 딜레이 시간을 설정하고 있다.

```java
@Override
public void run(String... args) throws Exception {
  System.out.println(OffsetDateTime.now().format(formatter));
  UserMessageDto message = createMessageDto();
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
```

아래는 전체 코드다.

```java
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
		sendMessageByPostProcessor(message);
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
```

<br>

## 소비자 코드

@RabbitListener 를 이용하는 로직을 작성하는 것은 일반적인 다른 방식과 다른 부분이 없다. 아래 예제 코드는 단순히 메시지를 전달받는 단순동작을 하는 예제코드다.

```java
@Service
public class UserMessageConsumerService {

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

	@RabbitListener(queues = "MESSAGE_DELAY_PUSH_QUEUE", messageConverter = "mqMessageConverter")
	public void receiveMessage(final UserMessageDto message){
		final String currentDateTime = OffsetDateTime.now().format(formatter);
		System.out.println(currentDateTime + " [메시지 데이터 수신] message = " + message.toString());
	}
}
```

