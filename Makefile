.PHONY: android-debug backend-dev backend-build backend-test compose-up compose-down

android-debug:
	./gradlew :app:assembleDebug

backend-dev:
	cd backend && npm run dev

backend-build:
	cd backend && npm run build

backend-test:
	cd backend && npm test

compose-up:
	docker compose up --build -d

compose-down:
	docker compose down
