# Образ содержит maven и JDK
FROM maven:3.9.9-eclipse-temurin-22

# Рабочая директория
WORKDIR /app

COPY pom.xml .

# Загрузка зависимостей
RUN mvn dependency:go-offline

COPY . .

# Все последующие команды в Dockerfile выполняются от имени root
USER root

# Делаем скрипт для запуска тестов в контейнере исполняемым
RUN chmod +x /app/run-tests-in-container.sh

# Выполняем скрипт для запуска тестов
CMD ["/app/run-tests-in-container.sh"]
