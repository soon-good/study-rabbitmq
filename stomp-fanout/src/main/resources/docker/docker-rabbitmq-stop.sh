# rabbitmq docker container 중지 및 볼륨 삭제 스크립트

name_rabbitmq_local='rabbitmq-local'

cnt_rabbitmq_local=`docker container ls --filter name=rabbitmq-local | wc -l`
cnt_rabbitmq_local=$(($cnt_rabbitmq_local -1))

if [ $cnt_rabbitmq_local -eq 0 ]
then
    echo "'$name_rabbitmq_local' 컨테이너가 없습니다. 삭제를 진행하지 않습니다."

else
    echo "'$name_rabbitmq_local' 컨테이너가 존재합니다. 기존 컨테이너를 중지하고 삭제합니다."
    docker container stop rabbitmq-local
    rm -rf ~/env/docker/mq-container/volumes/rabbitmq-local
    echo "\n'$name_rabbitmq_local' 컨테이너 삭제를 완료했습니다.\n"
fi
