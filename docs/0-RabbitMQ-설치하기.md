# RabbitMQ 설치하기

## 참고자료

- [Install - Debian and Ubuntu](https://www.rabbitmq.com/install-debian.html)
- [Installing on Debian and Ubuntu](https://www.rabbitmq.com/install-debian.html#manual-installation)
- [RabbitMQ packages have unmet dependencies](https://askubuntu.com/questions/1188699/rabbitmq-packages-have-unmet-dependencies)

<br>

## apt-key 

RabbitMQ 설치시에 Erlang 언어를 설치해야 하는데 이 때 apt-key 관련해서 문제가 있다.<br>

자세한 내용은 [Install - Debian and Ubuntu](https://www.rabbitmq.com/install-debian.html) 를 참고<br>

```bash
$ sudo apt-key adv --keyserver "hkps://keys.openpgp.org" --recv-keys "0x0A9AF2115F4687BD29803A206B73A36E6026DFCA"
$ sudo apt-key adv --keyserver "keyserver.ubuntu.com" --recv-keys "F77F1EDA57EBB1CC"
$ curl -1sLf 'https://packagecloud.io/rabbitmq/rabbitmq-server/gpgkey' | sudo apt-key add -
```

<br>

## 우분투 리포지터리 추가

공식문서에서 언급하기로는 ubuntu 20 부터는 `focal` 이라는 저장소를 사용해야 Erlang 을 설치할수 있다. 아래 명령어는 ubuntu20 에서 focal을 사용해야 한다는 부분의 명령어를 요약한 내용이다.

```bash
$ sudo tee /etc/apt/sources.list.d/rabbitmq.list <<EOF
# 나타나는 콘솔창에서 아래의 명령을 차례로 입력해준다. 
# 자세한 내용은 공식문서를 참고할것 

> deb http://ppa.launchpad.net/rabbitmq/rabbitmq-erlang/ubuntu focal main
> deb-src http://ppa.launchpad.net/rabbitmq/rabbitmq-erlang/ubuntu focal main
> deb https://packagecloud.io/rabbitmq/rabbitmq-server/ubuntu/ focal main
> deb-src https://packagecloud.io/rabbitmq/rabbitmq-server/ubuntu/ focal main
> EOF

가장 마지막은 EOF 라는 문자를 타이핑해서 빠져나오기
```

<br>

## RabbitMQ, apt-transport-https 설치

```bash
$ sudo apt-get update -y
$ sudo apt-get install rabbitmq-server -y --fix-missing
$ sudo apt-get install apt-transport-https
```

<br>

## RabbitMQ 구동

```bash
$ sudo service rabbitmq-server start
$ sudo service rabbitmq-server status
$ sudo service rabbitmq-server stop
```

<br>

