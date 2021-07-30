# RabbitMQ+Spring 개발시 접하게 되는 에러로그들



#### 참고자료

- [Problems and solutions encountered in springboot integration with RabbitMQ](https://www.programmersought.com/article/72524704920/)

<br>

#### Broker not available; cannot force queue declations during star: java.io.IOException

> [참고한 자료](https://www.programmersought.com/article/72524704920/#%E9%97%AE%E9%A2%982%EF%BC%9A%E8%BF%9E%E6%8E%A5%E6%8A%A5%E9%94%99%EF%BC%9ABroker%20not%20available%3B%20cannot%20force%20queue%20declarations%20during%20start%3A%20java.io.IOException)<br>

<br>

**에러 로그**<br>

```plain
2021-07-29 12:01:52.351  INFO 18063 --- [           main] o.s.a.r.l.SimpleMessageListenerContainer : Broker not available; cannot force queue declarations during start: java.io.IOException
```

<br>

**증상 & 해결**<br>

포트 번호를 잘못 지정해주어서 생기는 문제이다. 이 경우 래빗엠큐로의 접속포트가 15672로 세팅되어 있을 수 있는데, 15672 는 어드민 페이지의 포트이다. 5672 로 수정해주어야 한다.<br>

그런데, Amazon MQ의 경우 5671이 엔드포인트의 포트이다. 5671로 설정해주어야 잘 붙는다. 그리고 이렇게 포트를 잘 선택해도 결국에는 SimpleMessageListenerContainer 에러가 발생하게 된다.<br>

<br>



