#!/bin/bash

# Скрипт внутри контейнера

# Создаем папку для логов
mkdir -p /app/logs

{
  # Выводим сообщение о запуске тестов
  echo ">>> Running tests with profile: ${TEST_PROFILE}"
  # Запускаем тесты (-q не печатает лишние логи, -P запускает с указанным профилем)
  mvn test -q -P "${TEST_PROFILE}"
  # Выводим сообщение о запуске генерации отчета
  echo ">>> Running surefire-report:report"
  # Генерируем отчет
  mvn -DskipTests=true surefire-report:report
  # Вывод из скобок {} вместе с ошибками (2>&1) перенаправляем в /app/logs/run.log
} > /app/logs/run.log 2>&1
