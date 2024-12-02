# Stage 1: Build
FROM ubuntu:latest AS build

RUN apt-get update
RUN apt-get install openjdk-21-jdk -y

# Define o diretório de trabalho como o diretório raiz do projeto
WORKDIR /app

# Copia o arquivo pom.xml para o diretório atual
COPY Pergamo/pom.xml .

# Instala as dependências do Maven
RUN apt-get install maven -y

COPY Pergamo/. .

# Executa o Maven para construir o projeto
RUN mvn clean install

# Stage 2: Run
FROM openjdk:21-jdk-slim

EXPOSE 8080

# Copia o arquivo jar gerado na fase de construção para a nova imagem
COPY --from=build /app/target/demo2-0.0.1-SNAPSHOT.jar /app/app.jar

# Define o comando de entrada para executar o aplicativo quando o contêiner for iniciado
ENTRYPOINT ["java", "-jar", "/app/app.jar"]