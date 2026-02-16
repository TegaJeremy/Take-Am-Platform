package database

import (
	"fmt"
	"log"
	"os"
	"time"

	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
	"marketplace-service/models"
)

var DB *gorm.DB

func ConnectDB() {
	host := os.Getenv("DB_HOST")
	port := os.Getenv("DB_PORT")
	user := os.Getenv("DB_USER")
	password := os.Getenv("DB_PASSWORD")
	dbname := os.Getenv("DB_NAME")

	dsn := fmt.Sprintf(
		"host=%s user=%s password=%s dbname=%s port=%s sslmode=disable TimeZone=UTC",
		host, user, password, dbname, port,
	)

	var err error
	maxRetries := 10

	for i := 1; i <= maxRetries; i++ {
		log.Printf("⏳ Attempting database connection (attempt %d/%d)...", i, maxRetries)

		DB, err = gorm.Open(postgres.Open(dsn), &gorm.Config{
			Logger: logger.Default.LogMode(logger.Info),
		})

		if err == nil {
			log.Println("✅ Database connected successfully")
			break
		}

		log.Printf("❌ Database connection failed: %v", err)

		if i < maxRetries {
			log.Printf("⏰ Retrying in 3 seconds...")
			time.Sleep(3 * time.Second)
		} else {
			log.Fatal("🚨 Failed to connect to database after all retries")
		}
	}

	log.Println("🔄 Running database migrations...")
	err = DB.AutoMigrate(
		&models.Product{},
		&models.ProductImage{},
		&models.Cart{},
		&models.CartItem{},
		&models.Order{},
		&models.OrderItem{},
	)

	if err != nil {
		log.Fatal("Failed to migrate database:", err)
	}

	log.Println("✅ Database migrations completed successfully")
}

func GetDB() *gorm.DB {
	return DB
}