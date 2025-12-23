#!/bin/bash

echo ">>> Запуск minikube"
minikube start --driver=docker

kubectl create configmap selenoid-config --from-file=browsers.json=./kube/nbank-chart/files/browsers.json

echo ">>> Установка сервисов в Kubernetes-кластер"
helm install nbank ./kube/nbank-chart

echo ">>> Ожидание готовности подов"
kubectl wait --for=condition=available --timeout=300s deployment/frontend
kubectl wait --for=condition=available --timeout=300s deployment/backend
kubectl wait --for=condition=available --timeout=300s deployment/selenoid
kubectl wait --for=condition=available --timeout=300s deployment/selenoid-ui

echo ">>> Запуск проброса портов"
kubectl port-forward svc/frontend 3000:80 > /dev/null 2>&1 &
kubectl port-forward svc/backend 4111:4111 > /dev/null 2>&1 &
kubectl port-forward svc/selenoid 4444:4444 > /dev/null 2>&1 &
kubectl port-forward svc/selenoid-ui 8080:8080 > /dev/null 2>&1 &

echo ">>> Выполнено"
