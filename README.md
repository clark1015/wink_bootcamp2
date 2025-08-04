# 🚀 WinkBootCamp

**Spring Boot 기반 인증 시스템** - JWT, OAuth2, 이메일 인증을 지원하는 백엔드 API 서버입니다.

## ✨ 주요 특징

### 🎯 **비동기 메시징 시스템**
- **RabbitMQ 기반 이메일 서비스**: Producer/Consumer 패턴으로 결합도 최소화
- **비동기 처리**: 이메일 발송으로 인한 API 응답 지연 제거

### ⚡ **Redis 기반 토큰 관리**
- **인증번호 TTL 관리**: 5분 자동 만료
- **JWT 블랙리스트**: 로그아웃된 토큰 즉시 무효화
- **Refresh Token 저장**: 14일 TTL로 안전한 토큰 관리

### 🎨 **반응형 이메일 템플릿**
- **HTML 템플릿**: 깔끔한 디자인의 인증 이메일
- **반응형 디자인**: 모바일/데스크톱 지원

### 🐳 **Docker 컨테이너화**
- **일관된 배포 환경**: Docker Compose 기반 멀티 컨테이너
- **개발/운영 환경 통일**: 환경별 설정 관리

## 🏗️ 시스템 아키텍처

```
                    ☁️ AWS EC2 Instance
                          │
                    📊 GitHub Actions
                    🐳 Docker Deploy  
                          │
                    ┌─────────────▼───────────────┐
                    │     Spring Boot API         │
                    │      (Port: 8080)          │
                    │                             │
                    │  ┌─────────────────────┐    │
                    │  │ 🔐 Auth Controller  │    │
                    │  │ 📧 Email Controller │    │
                    │  │ 🛡️ JWT Filter       │    │
                    │  │ 🔑 OAuth2 Config    │    │
                    │  └─────────────────────┘    │
                    └─────────────┬───────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌────────▼────────┐    ┌─────────▼────────┐    ┌────────▼────────┐
│     MySQL       │    │      Redis       │    │   RabbitMQ      │
│   (Port: 3306)  │    │   (Port: 6379)   │    │  (Port: 5672)   │
│                 │    │                  │    │                 │
│ ┌─────────────┐ │    │ ┌──────────────┐ │    │ ┌─────────────┐ │
│ │ 👤 User DB  │ │    │ │ ⚡ Auth Code │ │    │ │ 📨 Email    │ │
│ │ 🔗 OAuth    │ │    │ │   (TTL: 5m)  │ │    │ │   Producer  │ │
│ └─────────────┘ │    │ │ 🔄 Refresh   │ │    │ │ 📬 Consumer │ │
└─────────────────┘    │ │    Tokens    │ │    │ │   Queue     │ │
                       │ │ ❌ JWT Black │ │    │ └─────────────┘ │
                       │ │    List      │ │    └─────────────────┘
                       │ └──────────────┘ │
                       └──────────────────┘
                                │
                    ┌───────────▼──────────┐
                    │   External Services  │
                    │                      │
                    │  ┌─────────────────┐ │
                    │  │ 📨 Gmail SMTP   │ │
                    │  │ 🟡 Kakao OAuth2 │ │
                    │  └─────────────────┘ │
                    └──────────────────────┘
```

### 🔄 **비동기 이메일 처리 플로우**
```
이메일 요청 → RabbitMQ Producer → Queue → Consumer → SMTP 발송
     ↓              (즉시 응답)        (비동기)     (백그라운드)
  200 OK 응답
```

## 🛠️ 기술 스택

### Backend & Architecture
- **Framework**: Spring Boot 3.5.4 (Java 21)
- **Security**: Spring Security + JWT
- **Database**: MySQL 8.0
- **Cache**: Redis 7.2 (TTL 기반 토큰 관리)
- **Message Queue**: RabbitMQ 3 (비동기 이메일 처리)
- **Email**: Spring Mail (Gmail SMTP)
- **Build Tool**: Gradle

### DevOps & Infrastructure  
- **Containerization**: Docker & Docker Compose
- **CI/CD**: GitHub Actions (자동 배포)
- **Cloud**: AWS EC2 
- **Registry**: GHCR (GitHub Container Registry)
- **Monitoring**: Spring Boot Actuator

### Authentication & Security
- **JWT**: Access Token (15분) + Refresh Token (14일)
- **OAuth2**: Kakao Social Login
- **Email Verification**: TTL 기반 인증코드 (5분)
- **Token Management**: Redis 블랙리스트 시스템

## 📦 핵심 기능 및 기술적 우수성

### 🔐 **고도화된 인증 시스템**
- **다중 인증 방식**: 이메일/비밀번호 + 카카오 OAuth2
- **JWT 기반 무상태 인증**: 확장성과 성능 최적화
- **토큰 생명주기 관리**: Access Token 자동 갱신
- **보안 강화**: Redis 기반 실시간 토큰 블랙리스트

### ⚡ **Redis 기반 토큰 관리**
```yaml
Redis 활용 영역:
├── 🔢 이메일 인증코드 (TTL: 5분)
├── 🔄 Refresh Token 저장 (TTL: 14일)  
└── ❌ JWT 블랙리스트 (로그아웃 토큰)
```

### 📨 **비동기 이메일 아키텍처** 
**RabbitMQ Producer/Consumer 패턴으로 결합도 최소화**

```java
// 🚀 비동기 이메일 발송 플로우
EmailAuthProducer → RabbitMQ Queue → EmailAuthConsumer → SMTP
     (즉시 응답)        (메시지 큐)      (백그라운드 처리)   (Gmail)
```

**주요 장점:**
- ✅ **API 응답속도 향상**: 이메일 발송 대기시간 제거
- ✅ **결합도 최소화**: 이메일 서비스와 인증 API 분리
- ✅ **안정성**: 이메일 발송 실패가 인증 프로세스에 영향 없음

### 🎨 **반응형 이메일 템플릿**
**사용자 경험을 고려한 깔끔한 HTML 이메일 디자인**

<img width="577" height="889" alt="image" src="https://github.com/user-attachments/assets/e49486a3-9a74-476f-8f78-5558fb287162" />


**주요 특징:**
- 📱 **반응형 디자인**: 모바일/데스크톱 환경 지원
- 🎨 **일관된 브랜딩**: WinkBootCamp 테마 적용
- 📧 **명확한 정보 전달**: 인증번호 가독성 최적화
- 🔒 **보안 안내**: 사용자 보안 인식 제고

### 🚀 **GitHub Actions CI/CD**
**자동화된 배포 파이프라인으로 개발 효율성 향상**

```yaml
배포 프로세스:
1. 🔍 Code Push → develop 브랜치
2. 🏗️ Docker Image Build  
3. 📦 GHCR (GitHub Container Registry) Push
4. 🚀 EC2 SSH 접속 → Docker Compose 배포
5. ✅ 컨테이너 자동 재시작
```

### ☁️ **AWS EC2 배포**
**클라우드 인프라 기반 안정적인 서비스 운영**

```yaml
배포 환경:
├── 🖥️ AWS EC2 Instance
├── 🐳 Docker Compose (멀티 컨테이너)
├── 📦 GHCR 이미지 저장소
└── 🔄 자동 배포 (develop 브랜치)
```

## 🛡️ 보안 특징

- **JWT 기반 무상태 인증**: 확장성과 성능 최적화
- **토큰 만료 정책**: Access Token 15분, Refresh Token 14일
- **블랙리스트 시스템**: 로그아웃된 토큰 즉시 무효화
- **이메일 인증**: 계정 생성시 이메일 소유권 검증
- **OAuth2 보안**: 카카오 표준 OAuth2 프로토콜 준수
- **환경 변수**: 민감한 정보 외부 설정 파일로 관리

## 🔄 데이터 플로우

### 회원가입 플로우
1. **이메일 인증코드 요청** → Redis에 코드 저장 (5분 TTL)
2. **RabbitMQ** → 이메일 발송 큐에 메시지 추가
3. **이메일 발송** → Gmail SMTP로 인증코드 전송
4. **코드 검증** → Redis에서 코드 확인
5. **회원가입** → MySQL에 사용자 정보 저장

### 로그인 플로우
1. **일반 로그인**: 이메일/비밀번호 검증
2. **소셜 로그인**: 카카오 OAuth2 연동
3. **JWT 발급**: Access Token + Refresh Token
4. **Redis 저장**: Refresh Token 저장 (14일 TTL)

### 로그아웃 플로우
1. **토큰 블랙리스트**: Access Token을 Redis 블랙리스트에 추가
2. **Refresh Token 삭제**: Redis에서 Refresh Token 제거
3. **토큰 무효화**: 이후 요청시 블랙리스트 확인

## 🚀 설치 및 실행

### 사전 요구사항
- Docker & Docker Compose
- Java 21 (로컬 개발시)
- Gmail 앱 비밀번호
- 카카오 Developer 계정

### 1. 저장소 클론
```bash
git clone <repository-url>
cd wink-bootcamp
```

### 2. 환경 변수 설정
`.env` 파일을 생성하고 다음 값들을 설정:

```env
# Database
MYSQL_PORT=3306
MYSQL_ROOT_PASSWORD=your_root_password
MYSQL_DATABASE=wink_bootcamp
MYSQL_USER=wink_user
MYSQL_PASSWORD=your_password

# Redis
REDIS_PORT=6379

# Spring Application
SPRING_PORT=8080
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/wink_bootcamp
SPRING_DATASOURCE_USERNAME=wink_user
SPRING_DATASOURCE_PASSWORD=your_password

# JWT
JWT_SECRET=your_very_secure_jwt_secret_key_here

# Email (Gmail)
GMAIL_USERNAME=your_email@gmail.com
GMAIL_PASSWORD=your_app_password

# OAuth2 (Kakao)
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret

# RabbitMQ
SPRING_RABBITMQ_HOST=rabbitmq
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest
```

### 3. Docker로 실행
```bash
# 모든 서비스 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f spring

# 서비스 중지
docker-compose down
```

### 4. 서비스 확인
- **API 서버**: http://localhost:8080
- **RabbitMQ 관리**: http://localhost:15672 (guest/guest)
- **Health Check**: http://localhost:8080/actuator/health

## 🔄 API 테스트 방법

현재는 백엔드 API만 구현되어 있으므로, 다음 도구들로 테스트할 수 있습니다:

### Postman/Insomnia 테스트 예시
```http
### 1. 이메일 인증코드 발송
POST http://localhost:8080/api/auth/email
Content-Type: application/json

{
  "email": "test@example.com"
}

### 2. 인증코드 확인
POST http://localhost:8080/api/auth/email/verify
Content-Type: application/json

{
  "email": "test@example.com",
  "code": "123456"
}

### 3. 회원가입
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123",
  "name": "홍길동"
}

### 4. 로그인
POST http://localhost:8080/api/auth/signin
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}

### 5. 카카오 로그인 (브라우저에서)
GET http://localhost:8080/oauth2/authorization/kakao
```

### cURL 테스트 예시
```bash
# 이메일 코드 발송
curl -X POST http://localhost:8080/api/auth/email \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'

# 로그인
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "password123"}'
```

## 📊 프로젝트 구조

```
src/main/java/hello/wink_bootcamp/
├── 📁 domain/
│   ├── 📁 auth/
│   │   ├── 📁 controller/       # 인증 API 컨트롤러
│   │   ├── 📁 dto/             # 요청/응답 DTO
│   │   ├── 📁 service/         # 인증 비즈니스 로직
│   │   └── CustomUserDetails.java
│   ├── 📁 email/
│   │   ├── 📁 dto/             # 이메일 메시지 DTO
│   │   └── 📁 service/         # 이메일 서비스
│   └── 📁 oauth/               # OAuth2 설정
└── 📁 global/
    ├── 📁 config/              # 설정 클래스들
    ├── 📁 jwt/                 # JWT 관련
    │   ├── 📁 filter/          # JWT 필터
    │   └── 📁 redis/           # Redis 토큰 관리
    └── 📁 security/            # Spring Security 설정

src/main/resources/
├── application.yaml             # 애플리케이션 설정
└── 📁 templates/               # 이메일 템플릿 (추후)

📁 docker/
├── docker-compose.yml          # 컨테이너 오케스트레이션
├── Dockerfile                  # Spring Boot 이미지
└── .env                       # 환경 변수
```

## 🔄 데이터 플로우

### 회원가입 플로우
1. **이메일 인증코드 요청** → Redis에 코드 저장 (5분 TTL)
2. **RabbitMQ** → 이메일 발송 큐에 메시지 추가
3. **이메일 발송** → Gmail SMTP로 인증코드 전송
4. **코드 검증** → Redis에서 코드 확인
5. **회원가입** → MySQL에 사용자 정보 저장

### 로그인 플로우
1. **일반 로그인**: 이메일/비밀번호 검증
2. **소셜 로그인**: 카카오 OAuth2 연동
3. **JWT 발급**: Access Token + Refresh Token
4. **Redis 저장**: Refresh Token 저장 (14일 TTL)

### 로그아웃 플로우
1. **토큰 블랙리스트**: Access Token을 Redis 블랙리스트에 추가
2. **Refresh Token 삭제**: Redis에서 Refresh Token 제거
3. **토큰 무효화**: 이후 요청시 블랙리스트 확인

## 🛡️ 보안 특징

- **JWT 기반 무상태 인증**: 확장성과 성능 최적화
- **토큰 만료 정책**: Access Token 15분, Refresh Token 14일
- **블랙리스트 시스템**: 로그아웃된 토큰 즉시 무효화
- **이메일 인증**: 계정 생성시 이메일 소유권 검증
- **OAuth2 보안**: 카카오 표준 OAuth2 프로토콜 준수
- **환경 변수**: 민감한 정보 외부 설정 파일로 관리

## 🔧 개발 환경 설정

### 로컬 개발
```bash
# 데이터베이스만 도커로 실행
docker-compose up -d mysql redis rabbitmq

# Spring Boot 로컬 실행
./gradlew bootRun
```

### 테스트
```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests AuthServiceTest
```

### 로그 확인
```bash
# 실시간 로그 모니터링
docker-compose logs -f spring

# 특정 서비스 로그
docker-compose logs redis
docker-compose logs rabbitmq
```

## 📈 모니터링

### Health Check
```bash
# 애플리케이션 상태 확인
curl http://localhost:8080/actuator/health

# 상세 헬스 체크
curl http://localhost:8080/actuator/health/db
curl http://localhost:8080/actuator/health/redis
```

### RabbitMQ 관리
- **URL**: http://localhost:15672
- **계정**: guest / guest
- **모니터링**: 큐 상태, 메시지 처리량, 에러율

## 🚨 자주 묻는 질문 (FAQ)

### **Q1: 이메일이 발송되지 않아요**
**A:** Gmail 설정을 확인해주세요
```bash
# ✅ 체크리스트
- Gmail 2단계 인증 활성화 필요
- 앱 비밀번호 생성 (일반 비밀번호 X)
- .env 파일의 GMAIL_PASSWORD에 앱 비밀번호 입력
- "보안 수준이 낮은 앱 액세스" 비활성화 상태여야 함
```

### **Q2: 카카오 로그인이 실패해요**
**A:** 카카오 개발자 콘솔 설정을 확인해주세요
```bash
# ✅ 확인사항
- Redirect URI: http://localhost:8080/login/oauth2/code/kakao
- 플랫폼 도메인 등록: localhost:8080
- REST API 키와 보안 키 정확히 설정
```

### **Q3: Redis 연결 오류가 발생해요**
**A:** Docker 컨테이너 상태를 확인해주세요
```bash
# 컨테이너 상태 확인
docker-compose ps

# Redis 로그 확인
docker-compose logs redis

# Redis 재시작
docker-compose restart redis
```

### **Q4: JWT 토큰 관련 오류가 나요**
**A:** JWT 설정을 확인해주세요
```bash
# ✅ 확인사항
- JWT_SECRET 길이: 최소 256bit (32글자 이상)
- 토큰 만료시간 설정 확인
- 시스템 시간 동기화 확인
```

### **Q5: Docker 컨테이너가 실행되지 않아요**
**A:** 포트 충돌을 확인해주세요
```bash
# 포트 사용 중인 프로세스 확인
lsof -i :8080  # Spring Boot
lsof -i :3306  # MySQL  
lsof -i :6379  # Redis
lsof -i :5672  # RabbitMQ

# 충돌시 포트 변경 또는 프로세스 종료
```

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 연락처

- **이메일**: seok626898@gmail.com
- **프로젝트 링크**: [GitHub Repository]

---

**Made with ❤️ by WinkBootCamp Team**
