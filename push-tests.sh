#!/bin/bash

IMAGE_NAME=nbank-tests
DOCKERHUB_USERNAME=username
TAG=latest
DOCKERHUB_TOKEN=token

echo ">>> Авторизация в Docker Hub с токеном"
echo $DOCKERHUB_TOKEN | docker login --username $DOCKERHUB_USERNAME --password-stdin

# Создаём buildx builder (или используем существующий)
docker buildx create --use --name multiarch-builder >/dev/null 2>&1 || docker buildx use multiarch-builder

echo ">>> Сборка multi-arch образа (amd64 + arm64)"
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t $IMAGE_NAME:$TAG \
  --load \
  .

echo ">>> Тегирование образа"
docker tag $IMAGE_NAME:$TAG $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG

echo ">>> Отправка образа в Docker Hub"
docker push $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG

echo ">>> Готово! Образ доступен как: docker pull $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"
