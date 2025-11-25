#!/bin/bash

IMAGE_NAME=nbank-tests
DOCKERHUB_USERNAME=username
TAG=latest
DOCKERHUB_TOKEN=token

echo ">>> Авторизация в Docker Hub с токеном"
echo $DOCKERHUB_TOKEN | docker login --username $DOCKERHUB_USERNAME --password-stdin

echo ">>> Тегирование образа"
docker tag $IMAGE_NAME $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG

echo ">>> Отправка образа в Docker Hub"
docker push $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG

echo ">>> Готово! Образ доступен как: docker pull $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"
