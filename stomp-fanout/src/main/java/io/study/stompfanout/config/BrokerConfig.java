package io.study.stompfanout.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class BrokerConfig implements WebSocketMessageBrokerConfigurer {

	@Value("${app.env.rabbitmq.stomp.host}")
	private String stompHost;

	@Value("${app.env.rabbitmq.stomp.port}")
	private int stompPort;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
		stompEndpointRegistry
			.addEndpoint("/ws-connect")
			.withSockJS();
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry brokerRegistry) {
		// 스프링 애플리케이션을 거쳐서 가는 경우에 대한 디렉터리
		brokerRegistry.setApplicationDestinationPrefixes("/app");
		brokerRegistry.setPathMatcher(new AntPathMatcher("."));

		// 잎에 topic, queue 가 붙은 친구들은 직통으로 RabbitMQ로 전달된다.
		// 전달해주는 매개체는 스프링 웹 애플리케이션 내의 BrokerRelay 객체
		// 해당 URL 을 초기에 등록해주는 객체는 바로 이곳에서 사용하고 있는 MessageBrokerRegistry 객체
		brokerRegistry
			// .enableSimpleBroker("/topic", "/queue")
			// .enableStompBrokerRelay("/topic", "/queue")
			.enableStompBrokerRelay("/queue")
			.setRelayHost(stompHost)
			.setRelayPort(stompPort)
			.setClientLogin("mqadmin")
			.setClientPasscode("mqadmin");

	}
}
