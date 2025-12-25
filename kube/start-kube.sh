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

echo ">>> Создание namespace для логирования"
kubectl create namespace logging --dry-run=client -o yaml | kubectl apply -f -

echo ">>> Установка Elasticsearch, Kibana"
kubectl apply -n logging -f logging/elasticsearch.yaml
kubectl apply -n logging -f logging/kibana.yaml

echo ">>> Ожидание готовности подов"
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=grafana -n monitoring --timeout=300s
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=prometheus -n monitoring --timeout=300s

kubectl wait --for=condition=ready pod -l app=elasticsearch -n logging --timeout=300s
kubectl wait --for=condition=ready pod -l app=kibana -n logging --timeout=300s

echo ">>> Установка Filebeat"
kubectl apply -n logging -f logging/filebeat.yaml

echo ">>> Ожидание готовности подов"
kubectl wait --for=condition=ready pod -l app=filebeat -n logging --timeout=120s

echo ">>> Ожидание инициализации шаблона индекса Filebeat"
sleep 10

echo  ">>> Проброс портов для Prometheus, Grafana, Kibana"
kubectl port-forward svc/monitoring-kube-prometheus-prometheus -n monitoring 3001:9090 > /dev/null 2>&1 &
kubectl port-forward svc/monitoring-grafana -n monitoring 3002:80 > /dev/null 2>&1 &
kubectl port-forward svc/kibana -n logging 3003:5601 > /dev/null 2>&1 &

echo ">>> Создание секретов для авторизации на бэкенде"
kubectl create secret generic backend-basic-auth --from-literal=username=admin --from-literal=password=admin -n monitoring
kubectl apply -f spring-monitoring.yaml

echo "Prometheus локально доступен по адресу: http://localhost:3001/"
echo "Grafana локально доступна по адресу: http://localhost:3002/"
echo "Kibana локально доступна по адресу: http://localhost:3003/"
echo "Для подключения логирования перейти в UI -> Stack Management -> Data Views -> Create Data View и указать filebeat-*"
echo ">>> Выполнено"
