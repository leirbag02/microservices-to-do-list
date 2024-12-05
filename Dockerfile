# Stage 1: Build
FROM ubuntu:latest AS build

# Atualiza o sistema e instala o OpenJDK 21 e Maven
RUN apt-get update && apt-get install -y openjdk-22-jdk maven

# Define o diretório de trabalho como o diretório raiz do projeto
WORKDIR /app

# Copia o arquivo pom.xml para resolver as dependências
COPY to-do-list/pom.xml .

# Baixa as dependências do Maven
RUN mvn dependency:go-offline

# Copia o restante do código-fonte do projeto
COPY to-do-list/. .

# Compila o projeto com o Maven
RUN mvn clean install

# Stage 2: Run
FROM openjdk:22-jdk-slim

# Expondo a porta 8080
EXPOSE 8080

# Copia o arquivo JAR gerado na fase de construção para o novo contêiner
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar /app/app.jar

# Define o comando de entrada para iniciar o aplicativo quando o contêiner for executado
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

