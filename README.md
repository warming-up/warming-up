# WarmingUp Monorepo

This repository is organized as an Android + backend monorepo.

## Projects

- `android/`: Android client project
- `backend/`: Spring Boot backend project

## Backend

Initial server setup:

```sh
cd backend
cp .env.example .env
docker-compose up --build
```

The server runs at `http://localhost:8080`.

Stop the server:

```sh
cd backend
docker-compose down
```
