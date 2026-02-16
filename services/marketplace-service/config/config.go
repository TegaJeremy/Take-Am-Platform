package config

import (
	"log"
	"os"
)

type Config struct {
	Port              string
	DBHost            string
	DBPort            string
	DBUser            string
	DBPassword        string
	DBName            string
	JWTSecret         string
	CloudinaryName    string
	CloudinaryAPIKey  string
	CloudinarySecret  string
	PaystackSecretKey string
}

func LoadConfig() *Config {
	config := &Config{
		Port:              getEnv("PORT", "8085"),
		DBHost:            getEnv("DB_HOST", "localhost"),
		DBPort:            getEnv("DB_PORT", "5432"),
		DBUser:            getEnv("DB_USER", "postgres"),
		DBPassword:        getEnv("DB_PASSWORD", ""),
		DBName:            getEnv("DB_NAME", "marketplace"),
		JWTSecret:         getEnv("JWT_SECRET", "default-secret"),
		CloudinaryName:    getEnv("CLOUDINARY_CLOUD_NAME", ""),
		CloudinaryAPIKey:  getEnv("CLOUDINARY_API_KEY", ""),
		CloudinarySecret:  getEnv("CLOUDINARY_API_SECRET", ""),
		PaystackSecretKey: os.Getenv("PAYSTACK_SECRET_KEY"),
	}

	log.Println("Configuration loaded successfully")
	return config
}

func getEnv(key, defaultValue string) string {
	value := os.Getenv(key)
	if value == "" {
		return defaultValue
	}
	return value
}