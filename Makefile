.PHONY: dev dev-down prod prod-down build logs logs-server logs-db clean

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

# 서버 빌드 (로컬)
build:
	cd server && ./gradlew bootJar

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
