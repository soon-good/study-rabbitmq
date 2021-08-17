# Queue - TTL 설정

나의 경우는 매번 익스체인지에 TTL을 설정하는 것이라고 자꾸 착각을 했었다.심지어 누가 물어봤을 때 잘못 알고 있는 지식을 전달했었다. TTL 설정은 큐에 지정하는 설정이다. 혼동하지 말자. 익스체인지에 하는 설정이 아니다. <br>

<br>

**TODO :: 별도의 시나리오/캡처와 함께 테스트한 결과도 역시 기록해둘 예정이다.**<br>

<br>

## 참고자료 

- [rabbitmq.com/ttl - Time To Live](https://www.rabbitmq.com/ttl.html) 

<br>

## 예제코드 

**TODO :: 예제 코드 링크도 적자. 어휴... 바빠서 엄두도 못냈는데, 이럴 때일 수록 마이크로 미터만큼씩 조금 더 자주 노력하면 언젠가는 모든게 다 되어 있다.**<br>

큐 하나에 지연 설정을 해서 TTL 을 걸었는데, 어떤 커넥션 또는 채널은 큐 전체에 설정된 TTL 을 오버라이딩해서 개별 채널에는 다른 TTL을 걸고 싶을 경우는 Message TTL 을 사용한다.<br>

일단 아래 예제는 큐에 Message TTL을 지정하는 방식이다. 이렇게 하면 큐에 접속된 모든 채널은 기본적으로 큐의 MessageTTL 을 따른다. (만약 특정 채널에 다른  TTL을 적용하고 싶다면, 채널이 큐에 접속을 생성할 때 따로 Message TTL 을 지정해서 큐에 접속하게끔 해준다)<br>

<br>

### 큐 설정 (MessagePushQueueConfig.java) 

```java
// ...
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
  
  // ... 

	@Bean(name = "messagePushExchange")
	public FanoutExchange messagePushExchange(){
		return ExchangeBuilder
			.fanoutExchange("MESSAGE_PUSH_EXCHANGE")
			.build();
	}

  // ...
  
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

### 데이터 발송(Push) 로직

```java
package io.study.studyrabbitmqfanout.config.queue.test;

// ... 

@Profile("test-rabbitmq-postgresql")
@Service
public class TestMessagePushProducerService {
	private final RabbitTemplate rabbitTemplate;
	private final FanoutExchange fanoutExchange;
	private final BatchingRabbitTemplate batchingRabbitTemplate;

	public TestMessagePushProducerService(
		@Qualifier("producerRabbitTemplate") final RabbitTemplate rabbitTemplate,
		@Qualifier("batchingRabbitTemplate") final BatchingRabbitTemplate batchingRabbitTemplate,
		@Qualifier("messagePushExchange") final FanoutExchange fanoutExchange
	){
		this.rabbitTemplate = rabbitTemplate;
		this.batchingRabbitTemplate = batchingRabbitTemplate;
		this.fanoutExchange = fanoutExchange;
	}

	@Scheduled(initialDelay = 1000, fixedRate = 500)
	public void sendMessageFixedRate(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
		TestMessageDto msg = TestMessageDto.builder()
			.message("메시지 " + OffsetDateTime.now().format(formatter))
			.build();
		System.out.println("[데이터 전송] " + msg.getMessage());
		rabbitTemplate.convertAndSend(fanoutExchange.getName(), "", msg);
	}
  
  // ...

}
```

<br>



