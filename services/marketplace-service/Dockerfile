# Build stage
FROM golang:1.23-alpine AS builder

WORKDIR /app

COPY . .

RUN go mod tidy && go mod download

RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -a -o main .

# Make executable HERE
RUN chmod +x main

# Final stage
FROM alpine:latest

RUN apk --no-cache add ca-certificates

WORKDIR /root/

COPY --from=builder /app/main .

EXPOSE 8085

CMD ["/root/main"]