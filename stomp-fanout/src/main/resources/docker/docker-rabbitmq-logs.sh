# rabbitmq docker container repl 접속 스크립트

name_rabbitmq_local='rabbitmq-local'

cnt_rabbitmq_local=`docker container ls --filter name=rabbitmq-local | wc -l`
cnt_rabbitmq_local=$(($cnt_rabbitmq_local -1))

if [ $cnt_rabbitmq_local -eq 0 ]
then
    echo "'$name_rabbitmq_local' 컨테이너가 없습니다. 컨테이너를 구동해주세요."

else
    docker logs -f rabbitmq-local
fi
