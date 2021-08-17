# 0. Spring AMQP 공식문서 SUMMARY

Spring AMQP 공식문서의 내용을 요약 및 시나리오기반 테스트&개념 정리

- [docs.spring.io - Spring AMQP](https://docs.spring.io/spring-amqp/docs/current/reference/html/)
- [rabbitmq.com - RabbitMQ Best Practices](https://www.rabbitmq.com/best-practices.html)

<br>

###  TODO

**큐 하나에 대한 최대 허용 메시지 건수**<br>

큐 하나에 허용되는 메시지의 갯수가 7만건이라는 정보를 공식 페이지 어디서 봤었는데 어디에 정리했는지 기억이 안난다.이거 다시 찾아보고 정리해야 한다.<br>

매번 까먹는다. 정리하자 으... 

<br>

**앞으로 더 정리해야 할 것들**<br>

- 소비자 측 - MassageListenerContainer Conf
  - batchSize 설정
  - 얼마까지만 받고 있다가 일정량을 받으면 한번에 DB에 insert 하는 방식을 구현할 때 좋은 지침이 될 것 같아 일단은 자료를 스크랩 해두었는데, 일정관리를 꼭 잘해서 적용해봐야 할 것 같다.!!!

- [Testing Support](https://docs.spring.io/spring-amqp/docs/current/reference/html/#testing)

<br>

### 생산자 측 - 배치(Batch) 메시지 전달방식 (Batching)

메시지를 Batching 방식으로 보내야 할 때가 있다. 예를 들면 10건의 단건 메시지를 한번에 모아서 익스체인지에 보내주는 방식이다. 이런 Batch 메시지 전달 방식에 대해서는 [메시지 Batching - BatchingRabbitTemplate](https://github.com/gosgjung/study-rabbitmq/blob/develop/docs/%EB%A9%94%EC%8B%9C%EC%A7%80-Batching-%EB%B0%9C%EC%86%A1-BatchingRabbitTemplate.md) 에 정리해두었다.

<br>

### 생산자 측 - Connection을 다수의 채널로 다중화

Connection 을 논리적인 개념인 채널로 만드는 과정에 대해 정리해야 한다. 아직 자료를 찾긴 했는데 정리는 언제할지 모르겠다. 아직까지는 필요하지는 않다.<br>

- [Channel](https://rabbitmq.github.io/rabbitmq-java-client/api/current/com/rabbitmq/client/Channel.html#waitForConfirms(long))
- [rabbitmq.com - Publishers](https://www.rabbitmq.com/publishers.html)

<br>

### 생산자/익스체인지 - 메시지 지연 전달방식(1)

자세한 내용과 개념, 예제는 [메시지 딜레이 전송.md](https://github.com/gosgjung/study-rabbitmq/blob/develop/docs/%EB%A9%94%EC%8B%9C%EC%A7%80-%EB%94%9C%EB%A0%88%EC%9D%B4-%EC%A0%84%EC%86%A1.md) 에 정리해 두었다.<br>

<br>

### 큐 - TTL 설정

`2021/08/01` : 별도의 테스트 시나리오/캡처와 함께 다른 문서에 따로 정리할 예정이다.<br>

자세한 내용과 개념, 예제는 [Queue-TTL설정.md](http://asdfsadfasdf) 에 따로 정리해두었다. (별도 문서 Merge 후 링크 반영하기)<br>

<br>

### 소비자측 - 배치 방식 리슨 (정리 필요)

자세한 내용은 [여기](https://github.com/gosgjung/study-rabbitmq/blob/develop/docs/%EB%A9%94%EC%8B%9C%EC%A7%80-Batching-%EB%B0%9C%EC%86%A1-BatchingRabbitTemplate.md) 에 정리해 두었다.<br>

- [@RabbitListener with Batching](https://docs.spring.io/spring-amqp/docs/current/reference/html/#receiving-batch)

메시지를 배치로 전송하면 메시지를 리슨하는 쪽에서도 배치로 받을 수 있다. 예를 들면 메시지를 보내는 측에서 `MessagePushDto` 를 10개를 모아서 익스체인지에 전달해놓았다고 해보자. 이렇게 보낸 메시지를 받을 때는 별다를 처리를 하지 않으면 똑같이 `MessagePushDto` 로 10번 받게 된다.<br>

그런데,  `MessagePushDto` 로 받는 방식 말고, `List<MessagePushDto>` 로 한번에 10개의 `MessagePushDto` 를 받고 싶을 때가 있다. 푸시되는 사용자 메시지 데이터를 이력으로 남기기 위해 DB에 insert 하는 동작이 짧은 시간에 매우 자주 이루어지게 되는 경우를 예로 들수 있다. 이 경우 사용자 메시지를 한번에 5000건씩 저장하는 방식으로 조금 Insert 작업이 빈도를 낮추는 것도 괜찮은 방법인 것 같다.<br>

메시지 리슨 동작을 배치 처리할 때는 보통  `SimpleRabbitListenerContainerFactory`  를 사용해 받도록 설정하는 편이다. 물론 다른 ListenerContainerFactory 역시도 사용할 수 있다. 이 내용에 대해서는 따로 문서에 정리해두었다. 정리한 문서의 링크는 이번주 금요일 쯤에 정리해두지 않을까 싶다.<br>



### RabbitTemplate 커넥션 관리 - ConnectionFactory

자세한 내용은 [RabbitTemplate-커넥션-관리---ConnectionFactory.md](https://github.com/gosgjung/study-rabbitmq/blob/develop/docs/RabbitTemplate-%EC%BB%A4%EB%84%A5%EC%85%98-%EA%B4%80%EB%A6%AC---ConnectionFactory.md) 에 정리해두었다.<br>



### Retry with Batch Listeners

- [Retry with Batch Listeners](https://docs.spring.io/spring-amqp/docs/current/reference/html/#resilience-recovering-from-errors-and-broker-failures)

이것도 정리해야 한다.<br>



### MessageListenerContainerConfiguration - batchSize 설정

- [Message Listener Container Configuration](https://docs.spring.io/spring-amqp/docs/current/reference/html/#containerAttributes)

<br>

### Resilience: Recovering from Errors and Broker Failures

스프링 클라우드 스택의 여러 스택들과 조화를 이룰수 있도록 이번 개발이 끝나면 또 고도화 하면서 준비하게 될 사항이 되지 않을까 싶다. 아래의  Rtry with Batch Listeners 역시 같은 내용이다.

- [Resilience : Recovering from Errors and Broker Failures](https://docs.spring.io/spring-amqp/docs/current/reference/html/#resilience-recovering-from-errors-and-broker-failures)

<br>



