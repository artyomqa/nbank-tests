# Образ содержит maven и JDK
FROM maven:3.9.9-eclipse-temurin-22

# Дефолтные значения аргументов
ARG TEST_PROFILE=api
ARG SERVICE_HOST=http://localhost
ARG SERVICE_PORT=4111
ARG SELENOID_URL=http://localhost:4444/wd/hub

# Рабочая директория
WORKDIR /app

COPY pom.xml .

# Загрузка зависимостей
RUN mvn dependency:go-offline

COPY . .

# Все последующие команды в Dockerfile выполняются от имени root
USER root

# В контейнере:
# Создаем папку для логов
# Выводим сообщение о запуске тестов
# Запускаем тесты (-q не печатает лишние логи, -P запускает с указанным профилем)
# Выводим сообщение о запуске генерации отчета
# Генерируем отчет
# Вывод из скобок {} вместе с ошибками (2>&1) перенаправляем в /app/logs/run.log
CMD /bin/bash -c " \
    mkdir -p /app/logs ; \
    { \
    echo '>>> Running tests with profile: ${TEST_PROFILE}' ; \
    mvn test -q -P ${TEST_PROFILE} ; \
    echo '>>> Running surefire-report:report' ; \
    mvn -DskipTests=true surefire-report:report ; \
    } > /app/logs/run.log 2>&1"