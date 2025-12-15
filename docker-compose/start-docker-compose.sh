#!/bin/bash
set -e

echo ">>> Останавливаем Docker Compose"
docker compose down

echo ">>> Загружаем все образы браузеров"
browsersFile="./config/browsers.json"
if [ ! -f "$browsersFile" ]; then
    echo "Файл $browsersFile не найден!"
    exit 1
fi

images=$(grep -oP '"image"\s*:\s*"\K[^"]+' "$browsersFile")

for img in $images; do
    echo "Загружаем образ: $img"
    docker pull "$img"
done

echo ">>> Запускаем Docker Compose"
docker compose up -d