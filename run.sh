#!/bin/bash

# Run script for XE Rate Alerts
# Starts both backend and frontend in separate terminal processes

# Get the project root directory
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Starting XE Rate Alerts..."
echo ""

# Start backend
echo "Starting backend on port 5180..."
cd "$ROOT_DIR/backend" && ./mvnw spring-boot:run &
BACKEND_PID=$!

# Wait for backend to start
echo "Waiting for backend to start..."
sleep 15

# Start frontend
echo "Starting frontend on port 5173..."
cd "$ROOT_DIR/frontend" && npm run dev &
FRONTEND_PID=$!

echo ""
echo "XE Rate Alerts is running!"
echo "Frontend: http://localhost:5173"
echo "Backend:  http://localhost:5180"
echo "Swagger:  http://localhost:5180/swagger-ui/index.html"
echo ""
echo "Press Ctrl+C to stop both servers"

# Wait for both processes
wait $BACKEND_PID $FRONTEND_PID