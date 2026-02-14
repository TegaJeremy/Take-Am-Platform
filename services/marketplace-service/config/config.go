package config

import (
	"os"
)

// Config holds all configuration
type Config struct {
	Port            string
	DBHost          string
	DBPort          string
	DBUser          string
	DBPassword      string
	DBName          string
	JWTSecret       string
	PaystackSecret  string
	CloudinaryURL   string
}

// LoadConfig loads configuration from environment variables
func LoadConfig() *Config {
	return &Config{
		Port:           getEnv("PORT", "8085"),
		DBHost:         getEnv("DB_HOST", "localhost"),
		DBPort:         getEnv("DB_PORT", "5432"),
		DBUser:         getEnv("DB_USER", "postgres"),
		DBPassword:     getEnv("DB_PASSWORD", "postgres123"),
		DBName:         getEnv("DB_NAME", "takeam_marketplace"),
		JWTSecret:      getEnv("JWT_SECRET", "default-secret-change-in-production"),
		PaystackSecret: getEnv("PAYSTACK_SECRET_KEY", ""),
		CloudinaryURL:  getEnv("CLOUDINARY_URL", ""),
	}
}

// getEnv gets environment variable with fallback
func getEnv(key, fallback string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return fallback
}