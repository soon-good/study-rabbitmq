package io.study.stompfanout.constant;

public class Cafe {
	public static class MenuItem{
		public static final String AMERICANO = "Americano";
	}
	public static class RabbitEnvironment{
		public static final String TOPIC_EXCHANGE_NAME = "CAFE_TOPIC_EXCHANGE";
		public static final String QUEUE_NAME = "CAFE_QUEUE";
		public static final String ROUTING_ALL_FULL_DEPTH = "cafe.#";
		public static final String ROUTING_ALL_SINGLE_WORD = "cafe.*";
		public static final String ROUTING_AMERICANO = "cafe.americano";
		public static final String ROUTING_LATTE = "cafe.latte";
	}
}
