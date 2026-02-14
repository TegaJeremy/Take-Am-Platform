# TakeAm Intake Service

Handles trader requests and agent intake process for TakeAm platform.

## Features

- **Traders**: Click "I have goods to sell"
- **Agents**: Accept requests, grade goods, close requests
- **Admin**: View all requests and statistics

## Installation
```bash
npm install
```

## Environment Variables

Copy `.env.example` to `.env` and configure:
```bash
cp .env.example .env
```

## Run Locally
```bash
npm run dev
```

## API Endpoints

### Trader Endpoints
- `POST /api/v1/trader-requests` - Create request
- `GET /api/v1/trader-requests/my` - Get my requests
- `GET /api/v1/trader-requests/:id` - Get request details

### Agent Endpoints
- `GET /api/v1/agent-requests/pending` - View pending requests
- `GET /api/v1/agent-requests/my-current` - Get current active request
- `POST /api/v1/agent-requests/:id/accept` - Accept request
- `POST /api/v1/agent-requests/:id/grade` - Submit grading
- `POST /api/v1/agent-requests/:id/close` - Close request

### Admin Endpoints
- `GET /api/v1/agent-requests/all` - Get all requests
- `GET /api/v1/agent-requests/stats` - Get statistics

## Port

Service runs on port `8084`