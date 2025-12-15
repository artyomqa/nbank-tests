#!/bin/bash
set -e

# Переменные окружения
SERVICE_HOST=http://77.221.154.242
SERVICE_PORT=4111
SELENOID_URL=http://77.221.154.242:4444/wd/hub

# Название образа с тестами
IMAGE_NAME=artyomqa/nbank-tests

# Проверка, передан ли тестовый профиль
if [ -z "$1" ]; then
    echo "Ошибка: не указан тестовый профиль. Возможные значения: api, ui."
    echo "Использование: ./run-tests-with-docker-compose.sh api"
    exit 1
fi

# Присваиваем тестовый профиль из аргумента скрипта
TEST_PROFILE="$1"

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

echo ">>> Подготовка к запуску тестов"
TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")
# Путь к директории, куда будут сохраняться: логи, отчёты Surefire, HTML-репорты
TEST_OUTPUT_DIR=./test-output/$TIMESTAMP

# Папка для логов
mkdir -p "$TEST_OUTPUT_DIR/logs"
# Папка для XML-отчётов Surefire.
mkdir -p "$TEST_OUTPUT_DIR/results"
# Папка для HTML-отчетов
mkdir -p "$TEST_OUTPUT_DIR/report"

echo ">>> Загружаем образ с тестами"
docker pull $IMAGE_NAME

echo ">>> Запуск тестов"
docker run --rm \
  --name nbank-tests-container \
  -v "$TEST_OUTPUT_DIR/logs":/app/logs \
  -v "$TEST_OUTPUT_DIR/results":/app/target/surefire-reports \
  -v "$TEST_OUTPUT_DIR/report":/app/target/site \
  -e TEST_PROFILE="$TEST_PROFILE" \
  -e SERVICE_HOST="$SERVICE_HOST" \
  -e SERVICE_PORT="$SERVICE_PORT" \
  -e SELENOID_URL="$SELENOID_URL" \
"$IMAGE_NAME"

echo ">>> Тесты выполнены. Останавливаем Docker Compose"
docker compose down -v

echo "Логи: $TEST_OUTPUT_DIR/logs/run.log"
echo "Результаты тестов: $TEST_OUTPUT_DIR/results"
echo "HTML-отчет: $TEST_OUTPUT_DIR/report"