# Spring-AMQP 공식문서 참고자료 네비게이션



##### 래빗MQ 커넥션 풀 생성 관련 자료

- [Choosing a Connection Factory](https://docs.spring.io/spring-amqp/docs/current/reference/html/#choosing-factory)

##### 지연된 익스체인지 사용 (Delayed Message Exchange)

- [Delayed Message Exchange](https://docs.spring.io/spring-amqp/docs/current/reference/html/#delayed-message-exchange)

exchange 설정 빈 또는 xml 에 아래와 같이 설정해준다. (java 코드는 라인수가 조금 되어서 xml로 설정함. xml 설정을 보면 java 설정도 가능하기에 그냥 xml 설정으로 정리함)

```xml
<rabbit:topic-exchange name="topic" delayed="true" />
```

<br>

ex 1) convertAndSend() 메서드 내에서 사용하기

```java
rabbitTemplate.convertAndSend(exchange, routingKey, "foo", new MessagePostProcessor() {
    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        message.getMessageProperties().setDelay(15000);
        return message;
    }
});
```

ex 2) 위의 소스 내에서 Properties 를 세팅하는 구문은 아래와 같이 수정 가능하다.

```java
MessageProperties properties = new MessageProperties();
properties.setDelay(15000);
template.send(exchange, routingKey,
        MessageBuilder.withBody("foo".getBytes()).andProperties(properties).build());
```



## BatchingRabbitTemplate

단건의 메시지를 하나의 큰 메시지로 묶어서 Exchange에 보낼때 사용한다. 보통 