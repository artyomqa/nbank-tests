#!/bin/bash

echo ">>> Запуск minikube"
minikube start --driver=docker

kubectl create configmap selenoid-config --from-file=browsers.json=./nbank-chart/files/browsers.json

echo ">>> Установка сервисов в Kubernetes-кластер"
helm install nbank ./nbank-chart

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

echo ">>> Установка Prometheus, Grafana"
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts || true
helm repo add elastic https://helm.elastic.co || true
helm repo update

helm upgrade --install monitoring prometheus-community/kube-prometheus-stack -n monitoring --create-namespace -f monitoring-values.yaml

echo ">>> Ожидание готовности подов"
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=grafana -n monitoring --timeout=300s
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=prometheus -n monitoring --timeout=300s

echo  ">>> Проброс портов для Prometheus и Grafana"
kubectl port-forward svc/monitoring-kube-prometheus-prometheus -n monitoring 3001:9090 > /dev/null 2>&1 &
kubectl port-forward svc/monitoring-grafana -n monitoring 3002:80 > /dev/null 2>&1 &

echo ">>> Создание секретов для авторизации на бэкенде"
kubectl create secret generic backend-basic-auth --from-literal=username=admin --from-literal=password=admin -n monitoring
kubectl apply -f spring-monitoring.yaml

echo ">>> Выполнено"
