#!/bin/bash
REPOSITORY=/opt/testapp
APP_NAME=upbrella-dev-deploy
cd $REPOSITORY

JAR_NAME=$(ls $REPOSITORY/build/libs/ | grep '.jar' | tail -n 1)
JAR_PATH=$REPOSITORY/build/libs/$JAR_NAME

CURRENT_PID=$(pgrep java -fl | awk '{print $1}')

if [ -z $CURRENT_PID ]
then
  echo "> 종료할것 없음."
else
  echo "> kill -9 $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
fi
echo "> $JAR_PATH 배포"

cd ~/be/build/libs

java -jar Yonsei-Golf-Server-0.0.1-SNAPSHOT.jar > app.log 2>&1 &