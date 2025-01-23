# Etapa 1: Construção do projeto
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copia os arquivos do projeto para o container
COPY . .

# Compila o projeto
RUN ./gradlew clean build -x test

# Etapa 2: Construção da imagem final
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia o JAR gerado na etapa de construção
COPY --from=build /app/build/libs/service-order-0.0.1-SNAPSHOT.jar app.jar

# Porta exposta pela aplicação
EXPOSE 8080

# Executa a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
