#!/bin/zsh
# rabbitmq docker container 구동 스크립트
name_rabbitmq_local='rabbitmq-local'
cnt_rabbitmq_local=`docker container ls --filter name=rabbitmq-local | wc -l`
cnt_rabbitmq_local=$(($cnt_rabbitmq_local -1))

if [ $cnt_rabbitmq_local -eq 0 ]
then
    echo "'$name_rabbitmq_local' 컨테이너를 구동시킵니다.\n"

    # rabbitmq 컨테이너 구동 & 볼륨 마운트
    docker container run --rm -d -p 15672:15672 -p 61613:61613 --name rabbitmq-local \
                --mount type=bind,source=$PWD/conf,target=/etc/rabbitmq \
                -e RABBITMQ_DEFAULT_USER=mqadmin \
                -e RABBITMQ_DEFAULT_PASS=mqadmin \
                -d rabbitmq:3.8-management

else
    echo "'$name_rabbitmq_local' 컨테이너가 존재합니다. 기존 컨테이너를 중지하고 삭제합니다."
    # 컨테이너 중지 & 삭제
    docker container stop rabbitmq-local

    # 컨테이너 볼륨 삭제
    rm -rf ~/env/docker/mq-container/volumes/rabbitmq-local/*
    echo "\n'$name_rabbitmq_local' 컨테이너 삭제를 완료했습니다.\n"

    # 디렉터리 존재 여부 체크 후 없으면 새로 생성
    DIRECTORY=~$USER/env/docker/mq-container/volumes/rabbitmq-local
    test -f $DIRECTORY && echo "볼륨 디렉터리가 존재하지 않으므로 새로 생성합니다.\n"

    if [ $? -lt 1 ]; then
      mkdir -p ~$USER/env/docker/mq-container/volumes/rabbitmq-local
      mkdir -p ~$USER/env/docker/mq-container/volumes/rabbitmq-local/var/lib/rabbitmq
    fi

    # rabbitmq 컨테이너 구동 & 볼륨 마운트
    docker container run --rm -d -p 15672:15672 -p 61613:61613 --name rabbitmq-local \
                -v ~/env/docker/mq-container/volumes/rabbitmq-local/var/lib/rabbitmq:/var/lib/rabbitmq \
                -v ~/rabbitmq-enabled-plugins:/etc/rabbitmq/enabled_plugins \
                -e RABBITMQ_DEFAULT_USER=mqadmin \
                -e RABBITMQ_DEFAULT_PASS=mqadmin \
                -d rabbitmq:3.8-management

fi
