.PHONY: dev dev-down prod prod-down build-server build-mobile build-web build-all test-server test-mobile test-web test-all lint-server lint-mobile logs logs-server logs-db clean

# 개발 환경
dev:
	docker compose up --build -d

dev-down:
	docker compose down

# 운영 환경
prod:
	docker compose -f docker-compose.prod.yml up --build -d

prod-down:
	docker compose -f docker-compose.prod.yml down

# 로그
logs:
	docker compose logs -f

logs-server:
	docker compose logs -f server

logs-db:
	docker compose logs -f db

# 정리
clean:
	docker compose down -v
	cd server && ./gradlew clean

# === 영역별 빌드 ===
build-server:
	cd server && ./gradlew bootJar

build-mobile:
	cd mobile && ./gradlew build

build-web:
	cd web && npm run build

build-all: build-server build-mobile build-web

# === 영역별 테스트 ===
test-server:
	cd server && ./gradlew test

test-mobile:
	cd mobile && ./gradlew allTests

test-web:
	cd web && npm test

test-all: test-server test-mobile test-web

# === 영역별 lint ===
lint-server:
	cd server && ./gradlew ktlintCheck

lint-mobile:
	cd mobile && ./gradlew ktlintCheck
