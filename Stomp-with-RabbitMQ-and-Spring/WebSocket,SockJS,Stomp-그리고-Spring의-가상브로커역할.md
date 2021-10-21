## 참고자료

- Spring Docs | WebSockets
  - Spring Boot 2.5.5 (GA) 공식문서에서 안내하는 Spring WebSocket 버전에 대한 Spring 버전은 5.3.1
  - https://docs.spring.io/spring-framework/docs/5.3.10/reference/html/web.html#websocket 

<br>

## WebSocket

웹소켓은 HTTP와는 다른 TCP 프로토콜이다. 하지만, HTTP 에서 작동하도록 설계되었다. 

80, 443 포트 사용, Web 에서 지정한 기존 방화벽 규칙을 재사용 가능하다.

클라 - 서버 간의 양방향 & 이중화(duplex) 통신을 지원

<br>

## When to Use Websocket

> 공식문서를 보다가 너무 밑에 있는 항목이어서 나중에야 확인하게 된 항목이다. 일단 파파고 번역만 정리해두었는데, 나중에 다시 정리해야 할 것 같다.

feat. 파파고

웹소켓은 웹 페이지를 역동적이고 상호작용하게 만들 수 있다. 그러나 대부분의 경우 Ajax와 HTTP 스트리밍 또는 긴 폴링을 함께 사용하면 간단하고 효과적인 솔루션을 제공할 수 있습니다.

예를 들어 뉴스, 메일 및 소셜 피드는 동적으로 업데이트해야 하지만 몇 분마다 업데이트해도 전혀 문제가 없을 수 있습니다. 반면에 협업, 게임 및 금융 앱은 실시간에 훨씬 더 가까워야 합니다.

대기 시간만으로는 결정적인 요소가 아닙니다. 메시지 볼륨이 상대적으로 낮은 경우(예: 네트워크 장애 모니터링) HTTP 스트리밍 또는 폴링을 통해 효과적인 솔루션을 제공할 수 있습니다. 이는 웹소켓의 최적 활용 사례를 만드는 짧은 대기 시간, 높은 주파수 및 높은 볼륨의 결합이다.

또한 인터넷을 통해 사용자가 제어할 수 없는 제한적인 프록시는 업그레이드 헤더를 전달하도록 구성되지 않았거나 유휴 상태로 보이는 장기간 연결을 닫기 때문에 WebSocket 상호 작용을 금지할 수 있습니다. 이는 방화벽 내의 내부 애플리케이션에 웹소켓을 사용하는 것이 공용 애플리케이션에 대한 것보다 더 간단한 결정임을 의미한다.

<br>

## 웹소켓 요청 Header

```yaml
GET /spring-websocket-portfolio/portfolio HTTP/1.1
Host: localhost:8080
Upgrade: websocket --- (1)
Connection: Upgrade  --- (2)
Sec-WebSocket-Key: Uc9l9TMkWGbHFD2qnFHltg==
Sec-WebSocket-Protocol: v10.stomp, v11.stomp
Sec-WebSocket-Version: 13
Origin: http://localhost:8080
```

<br>

**(1) Upgrade**

웹 소켓의 Upgrade 헤더를 upgrade로 두거나, websocket으로 두어서 웹소켓 요청을 수행할 수 있다.

<br>

**(2) Connection**

웹 소켓을 사용할 때 Connection 필드에 Upgrade 를 지정해준다.

 <br>

## 웹소켓 응답 Body (정상 응답=200 일 경우)

```yaml
HTTP/1.1 101 Switching Protocols 
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Accept: 1qVdfYHU9hPOl4JYYNXF623Gzn0=
Sec-WebSocket-Protocol: v10.stomp
```

Http Upgrade 리퀘스트를 정상적으로 처리해서 클라와 서버간의 핸드쉐이크가 정상적으로 수행되고 나면, TCP 소켓은 클라이언트와 서버가 메시지를 계속 송수신할 수 있도록 열려있는 상태가 된다.<br>

만약, 웹소켓 서버를 웹 서버(Nginx) 뒤에서 실행되도록 할 경우 웹 소켓 업그레이드 요청을 웹 서버(Nginx)로 전달해야 한다. 마찬가지로 애플리케이션을 클라우드 환경에서 운영하고 있는 경우, WebSocket 지원과 관련된 클라우드 제공자의 지침을 확인하고 따라야 한다.<br>

<br>

## HTTP vs 웹소켓 

웹소켓은 HTTP 요청으로 커넥션이 생성되고, HTTP와 호환되지만. HTTP, 웹소켓은 아키텍처가 서로 다르고, 애플리케이션 프로그래밍 모델 역시 다르다.

### **HTTP**

HTTP 및 REST 에서 애플리케이션은 URL을 모델링해서 사용하는데, 클라이언트와 서버애플리케이션이 통신을 할때 Request - Response  스타일의 통신 방식으로 URL 에 액세스 한다. 서버에는 HTTP URL 처리기, HTTP Method/헤더 처리기를 가지고 라우트한다.

### **웹소켓**

**초기 커넥션 생성 후 커넥션 내에서 통신**

초기 커넥션 생성을 위한 하나의 URL만 사용된다. 이후 모든 응용 프로그램 메시지는 동일한 TCP 연결에서 흐른다. 통신이 필요할 때마다 Request를 보내고 Response를 받는 HTTP 와 비교해보면 조금 대조적인 방식이다. 이것은 완전히 다른 비동기 이벤트 기반 메시징 아키텍처다.

**low level 프로토콜**

웹소켓은 low level 프로토콜이다. HTTP와 달리 메시지의 내용에 대해 어떠한 의미도 정의하지 않는다. 클라이언트와 서버 사이에 연동 규약을 정해놓지 않으면, 메시지가 라우팅되거나 처리되지 않는다.

**STOMP 프로토콜 사용**

웹 소켓 클라이언트 - 서버 간에 HTTP기반 핸드쉐이크 요청을 할때 Sec-WebSocket-Protocol 헤더를 사용해서 STOMP 프로토콜의 사용을 협상할 수 있다. STOMP 프로토콜은 더 높은 수준의 메시징 프로토콜이다. 만약 이것이 없다면, 따로 규약을 마련해야 한다.<br>

<br>

## 웹 소켓 기본 설정

```java
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
          .addHandler(myHandler(), "/myHandler")
          .setAllowedOrigins("https://mydomain.com");
    }

    @Bean
    public WebSocketHandler myHandler() {
        return new MyHandler();
    }

}
```

<br>

## SockJS Fallback

> 참고: Fallback = 대비책
>
> SockJS 는 익스플로러 등에서 특정 커넥션 유휴에 대해 차단하는 설정이라든가, 여러가지 속성에 대비해서 사용되는 옵션이다.

인프라 또는 네트워크 설정 등으로 인해 제한적인 프록시를 가지는 경우 WebSocket HTTP Request 시 Upgrade 헤더를 전달하지 못하게 되어 잇거나, 유휴상태로 보이는 장기간 연결(WebSocket의 경우 커넥션 하나로 지속적인 통신)을 닫기 때문에, WebSocket 상호작용이 차단될 수 있다.<br>

이런 경우 해결책은 WebSocket을 흉내낸(에뮬레이션) HTTP 통신을 사용하는 것이다. WebSocket HTTP Connection 을 먼저 생성한 다음, WebSocket 상호 작용을 애뮬레이션 해서 동일한 애플리케이션 레벨 API 를 노출하는 HTTP 기반 통신 기술로 표현하는 방식이다. 대표적으로 SockJS가 존재.<br>

서블릿 스택에서 스프링 프레임워크는 SockJS 프로토콜에 대한 서버/클라이언트 지원을 모두 제공한다.<br>

<br>

```java
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
          .addHandler(myHandler(), "/myHandler")
          .setAllowedOrigins("https://mydomain.com")
          // fallback 
          .withSockJS();
    }

    @Bean
    public WebSocketHandler myHandler() {
        return new MyHandler();
    }

}
```

<br>

## IE8, IE9 지원

자세한 내용은 아래 링크를 참고<br>

https://docs.spring.io/spring-framework/docs/5.3.10/reference/html/web.html#websocket

<br>

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/portfolio").withSockJS()
                .setClientLibraryUrl("http://localhost:8080/myapp/js/sockjs-client.js");
    }

    // ...

}
```

<br>

## HeartBeats

바로 위에서 살펴봤듯이, 특정 유휴 커넥션 등을 자동으로 회수하거나 프록시 등의 설정이 있을 때 SockJS 프로토콜을 사용한다. SockJS 는 서버가 프록시가 연결이 끝났다는 결론을 내리지 않도록 하트비트(heart beat)를 서버에 주기적으로 보낸다.(=캔유 필 마이 하트 빗 을 기억한다면… 이해가 쉬움)<br>

하트비트에 관련된 별다른 설정을 하지 않았을 경우 SockJS는 해당 연결에 다른 메시지가 전송되지 않았다는 가정하에 25초 간격으로 전송된다. 이 25초 값은 공용 인터넷 프로그램에 대한 IETF 권장사항 등에 따른 기본 설정값이다.<br>

스프링에서는 Spring SockJS 지원을 통해 하트비트 태스크를 예약하도록 Task Scheduler 를 구성할 수 있다. Task Scheduler 는 사용가능한 프로세서 수를 기준으로 기본 설정을 사용하는 스레드 풀로 백업된다. 특정 필요에 따라 설정을 사용자화(커스터마이징) 해야 한다.<br>

 <br>

## STOMP

> Stomp 프로토콜에 대한 자세한 설명은 내일 중으로 세부 문서를 통해 정리할 예정
>
> https://docs.spring.io/spring-framework/docs/5.3.10/reference/html/web.html#websocket-stomp

 <br>

### 장점

- STOMP 를 하위 프로토콜로 사용하면, 스프링,스프링시큐리티는 원시적인 웹소켓을 사용하는 것보다 더 풍부한 프로그래밍 모델을 제공가능하다.
- 커스텀 메시징 프로토콜(규약), 메시지 포맷을 만들 필요가 없다.
- STOMP 클라이언트를 사용하는 것이 가능하다.
- subscription 과 broadcast 를 관리하기 위해 여러가지 종류의 메시지 브로커(RabbitMQ, ActiveMQ, Kafka, etc)를 사용가능하다.
- 애플리케이션의 웹소켓 관련 로직들이 @Controller 인스턴스 들로 구성될 수 있다. 메세지는 STOMP 의 destination header 를 이용해서 @Controller 로 라우팅 할 수 있다. 만약 Stomp 를 사용하지 않는다면, WebSocket 메시지를 WebSocketHandler를 이용해서 원시적으로 처리해야 한다.

 <br>

## Enable Stomp

Stomp 를 웹 소켓에 지원되도록 하려면 아래의 두 모듈을 추가해줘야 한다.

- spring-messaging
- spring-websocket

<br>

위의 두 의존성을 추가하고 나면, SockJS Fallback 에 대한 Stomp Endpoint 를 추가할 수 있게 된다. <br>

```java
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer { -- (0)

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/portfolio").withSockJS();  --- (1)
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app"); --- (2)
        config.enableSimpleBroker("/topic", "/queue");    --- (3)
    }
}
```

<br>

- (0)
  - `WebSocketMessageBrokerConfigurer` 를 implements 했다.
- (1) 
  - /portfolio 는 웹소켓/SockJS 클라이언트가 웹소켓 핸드쉐이크를 위해 접속해야하는 HTTP URL 에 대한 엔드포인트다.
  - 매개변수로 `StompEndpointRegistry registry` 가 사용되었음에 주목하자.
- (2) 
  - destination header 가 `/app` 으로 시작되는 STOMP 메시지들은 `@MessageMapping` 이 적용된 메서드 들에 라우팅된다. `@MessageMapping` 은 `@Controller` 클래스 내의 메서드 들에 선언하는 어노테이션이다. 
- (3)
  - subscription, broadcasting 을 위한 기본 내장 브로커로, destination header가 `/topics` , `/queue` 로 시작되는 메시지들을 라우팅한다.

<br>

## Flow of Messages

Message Flow를 구성하는 방식은 두가지가 있다.

- JMS/메모리기반 스프링 기본 메시지 큐 사용 방식
- RabbitMQ, ActiveMQ, Kafka 기반 메시지 큐 사용 방식

 <br>

메시지 큐 구성시 사용되는 요소는 아래와 같다.

- Message
  - header, payload 기반의 기본적인 메시지 표현 
- MessageHandler
  - Message를 핸들링하는 하나의 Contract
- MessageChannel
  - 메시지를 보내는 하나의 Contract. 
  - MessageChannel 은 Message를 보내는 프로듀서와 컨슈머 사이의 약한 결합을 가능하도록 한다.
- SubscribableChannel
  - MesssageHandler 를 subscribe 하는 subscriber 들과 MessageChannel 로 구성된다.
- ExecutorSubscribableChannel
  - 메시지 전송에 사용되는 executor 를 사용하는 SubscribableChannel 이다.

 <br>

### JMS/메모리기반 스프링 기본 메시지 큐 사용 방식

JMS나 메모리기반 스프링 기본 메시지큐를 사용할 경우의 플로우는 아래와 같다.<br>

![이미지](https://docs.spring.io/spring-framework/docs/5.3.10/reference/html/images/message-flow-simple-broker.png)

<br>

- clientInboundChannel : 웹소켓 클라이언트 Request → MessageHandler
- clientOutboundChannel : 서버메시지를 웹소켓 클라이언트에 전달하기 위해 사용 (SimpleBroker → response)
- brokerChannel : 서버측 코드 → 메시지 브로커 로 메시지를 전송하기 위해 쓰이는 채널 (/topic, broker → SimpleBroker)

<br>

### RabbitMQ, ActiveMQ, Kafka 기반 메시지 큐 사용 방식

메시지의 브로드캐스팅과 Subscription 을 관리하는 외부 브로커를 사용할때의 다이어그램<br>

![이미지](https://docs.spring.io/spring-framework/docs/5.3.10/reference/html/images/message-flow-broker-relay.png)

<br>

스프링 내장 메시지큐를 사용할 때와 다른 점은 빨간 테두리를 친 영역인 StompBrokerRelay 와 관련된 부분이다.

자세히 살펴보면 아래와 같다.

- request → /topic → StompBrokerRelay → Message Broker
  - 클라이언트로부터 오는 Request가 topic을 통해서 바로 StompBrokerRelay 로도 향하는 것을 확인 가능하다.
- SimpAnnotationMethod → broker → /topic → StompBrokerRelay → Message Broker
  - 서버에서 생성한 메시지를 서버 내의 broker 가 /topic 을 통해 StompBrokerRelay 에 라우팅하고 있다.
  - 자세히 보면 StompBrokerRelay, broker 의 색상이 고동색으로 동일하다. StompBrokerRelay 와 broker 는 메시지 브로커에서 제공해주는 라이브러리 구현체임을 알 수 있다.

<br>