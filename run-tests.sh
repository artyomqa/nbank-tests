#!/bin/bash

# Переменные окружения
SERVICE_HOST=http://109.120.135.36
SERVICE_PORT=4111
SELENOID_URL=http://109.120.135.36:4444/wd/hub
# Имя образа
IMAGE_NAME=nbank-tests
# Имя контейнера
CONTAINER_NAME=nbank-tests-container
# Аргумент скрипта, по умолчанию api (выполняемый профиль)
TEST_PROFILE=${1:-api}
# Сохраняем в переменную текущую дату и время
TIMESTAMP=$(date +"%Y-%m-%d_%H-%M-%S")
# Путь к директории, куда будут сохраняться: логи, отчёты Surefire, HTML-репорты
TEST_OUTPUT_DIR=./test-output/$TIMESTAMP

echo ">>> Запуск сборки образа"
docker build -t $IMAGE_NAME .

# Папка для логов
mkdir -p "$TEST_OUTPUT_DIR/logs"
# Папка для XML-отчётов Surefire.
mkdir -p "$TEST_OUTPUT_DIR/results"
# Папка для HTML-отчетов
mkdir -p "$TEST_OUTPUT_DIR/report"

# Запуск контейнера с автотестами
# --rm удаляем контейнер после прохождения тестов
# -v монтируем папки для сохранения результатов тестов
# -e передаем переменные окружения в контейнер
echo ">>> Запуск тестов"
docker run --rm \
  --name "$CONTAINER_NAME" \
  -v "$TEST_OUTPUT_DIR/logs":/app/logs \
  -v "$TEST_OUTPUT_DIR/results":/app/target/surefire-reports \
  -v "$TEST_OUTPUT_DIR/report":/app/target/site \
  -e TEST_PROFILE="$TEST_PROFILE" \
  -e SERVICE_HOST="$SERVICE_HOST" \
  -e SERVICE_PORT="$SERVICE_PORT" \
  -e SELENOID_URL="$SELENOID_URL" \
$IMAGE_NAME

# Вывод результатов
echo ">>> Выполнение тестов завершено"
echo "Логи: $TEST_OUTPUT_DIR/logs/run.log"
echo "Результаты тестов: $TEST_OUTPUT_DIR/results"
echo "HTML-отчет: $TEST_OUTPUT_DIR/report"
