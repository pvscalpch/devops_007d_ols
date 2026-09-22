# syntax=docker/dockerfile:1

# ---------------------------------------------------------------
# Etapa 1: compilacion
# Usa una imagen con Maven y JDK completo. Solo vive durante el
# build: nada de esta etapa llega a la imagen final.
# ---------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Se copia primero el pom y se resuelven las dependencias.
# Asi un cambio en el codigo no obliga a volver a descargar Maven
# entero: Docker reutiliza esta capa mientras el pom no cambie.
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src

# Las pruebas NO corren aqui: necesitan MySQL y se ejecutan en su
# propio job del pipeline, que si tiene una base de datos.
RUN mvn -B -ntp clean package -DskipTests

# ---------------------------------------------------------------
# Etapa 2: ejecucion
# Imagen minima, solo con el JRE. Sin Maven, sin JDK, sin codigo
# fuente.
# ---------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine

LABEL org.opencontainers.image.title="ms-auth-user" \
      org.opencontainers.image.description="Microservicio de autenticacion de CarMeet" \
      org.opencontainers.image.source="https://github.com/pvscalpch/devops_007d_ols"

# curl para el HEALTHCHECK, y un usuario sin privilegios para no
# ejecutar la aplicacion como root.
RUN apk add --no-cache curl \
 && addgroup -S spring \
 && adduser -S spring -G spring

WORKDIR /app
COPY --from=build /build/target/*.jar app.jar

USER spring
EXPOSE 8090

# Docker consulta el endpoint publico de salud para saber si el
# contenedor esta realmente operativo, no solo encendido.
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:8090/api/health || exit 1

# MaxRAMPercentage en vez de un -Xmx fijo: la JVM se ajusta al
# limite de memoria que le asigne el orquestador.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
