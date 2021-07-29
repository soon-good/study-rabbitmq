# RabbitMQ 공식 문서 네비게이션



**채널 vs 커넥션**

- [What is the relationship between connections and channels in RabbitMQ?](RabbitMQ 공식 문서 네비게이션)
- 요약
  - Connections can multiplex over a single TCP connection, meaning that an application can open "lightweight connections" on a single connection. This "lightweight connection" is called a channel. Each connection can maintain a set of underlying channels.
- ![이미지](https://www.cloudamqp.com/img/blog/channel-in-connection.jpg)





