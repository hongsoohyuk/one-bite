# ===== Stage 1: Build =====
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Gradle wrapper & 설정 먼저 복사 (캐시 활용)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x gradlew

# 의존성 다운로드 (소스 변경 시 캐시 재사용)
RUN ./gradlew dependencies --no-daemon || true

# 소스 복사 & 빌드
COPY src src
RUN ./gradlew bootJar --no-daemon

# ===== Stage 2: Runtime =====
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
