#!/usr/bin/env bash

PROJECT_ROOT="/home/ubuntu/shoesbox/deploy"
JAR_FILE="$PROJECT_ROOT/ShoesBox-v0.1a.jar"

APP_LOG="$PROJECT_ROOT/application.log"
ERROR_LOG="$PROJECT_ROOT/error.log"
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

JAR_NAME=$(basename $JAR_FILE)
echo "> build 파일명: $JAR_NAME" >> $DEPLOY_LOG

TIME_NOW=$(date +%c)

# build 파일 복사
echo "$TIME_NOW > $JAR_FILE 파일 복사" >> $DEPLOY_LOG
cp $PROJECT_ROOT/build/libs/*.jar $JAR_FILE

# 실행중인 app pid 확인하고 있으면 종료
echo "> 현재 실행중인 애플리케이션 pid 확인" >> $DEPLOY_LOG
CURRENT_PID=$(pgrep -f "$JAR_NAME")

if [ -z "$CURRENT_PID" ]
then
  echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다." >> $DEPLOY_LOG
else
  echo "> kill -15 $CURRENT_PID"
  kill -15 "$CURRENT_PID"
  sleep 5
fi

# jar 파일 실행
echo "$TIME_NOW > $JAR_FILE 파일 실행" >> $DEPLOY_LOG
nohup java -jar $JAR_FILE > $APP_LOG 2> $ERROR_LOG &

CURRENT_PID=$(pgrep -f $JAR_FILE)
echo "$TIME_NOW > 실행된 프로세스 아이디 $CURRENT_PID 입니다." >> $DEPLOY_LOG
