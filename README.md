# Note App

Spring Boot REST API for storing notes in MongoDB. Pick the workflow that suits you.

## Docker Compose

- Requirement: Docker Engine 24+ (Compose plugin included).
- Command:
  ```
  docker compose up --build
  ```
  Compose builds the image, wires MongoDB, and exposes the API on http://localhost:8080.

## Local Gradle Run

1. Make sure MongoDB 7+ is running locally (quick option: `docker run -d --name noteapp-mongo -p 27017:27017 mongo:7`).
2. Config the required variables:
   ```
   SPRING_PROFILES_ACTIVE=dev
   SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/noteapp
   ```
3. Start the app with `./gradlew bootRun` and visit http://localhost:8080.
4. Run tests via `./gradlew test`.
