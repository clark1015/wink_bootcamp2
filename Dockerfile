# 1단계: 빌드 컨테이너 (Gradle + JDK 21)
FROM gradle:8.5.0-jdk21 AS builder
WORKDIR /app

# 캐시용 파일만 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle build -x test --no-daemon || return 0

# 전체 소스 복사 후 실제 빌드
COPY . .
RUN gradle build -x test --no-daemon

# 2단계: 실행 컨테이너 (JDK만 있음)
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]