package database

import (
	"context"
	"fmt"
	"log"
	"net"
	"os"
	"time"

	"marketplace-service/models"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/stdlib"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

var DB *gorm.DB

func ConnectDB() {
	host := os.Getenv("DB_HOST")
	port := os.Getenv("DB_PORT")
	user := os.Getenv("DB_USER")
	password := os.Getenv("DB_PASSWORD")
	dbname := os.Getenv("DB_NAME")
	env := os.Getenv("APP_ENV")

	sslmode := "require"
	if env == "local" || host == "localhost" || host == "127.0.0.1" {
		sslmode = "disable"
	}

	dsn := fmt.Sprintf(
		"host=%s user=%s password=%s dbname=%s port=%s sslmode=%s TimeZone=UTC",
		host, user, password, dbname, port, sslmode,
	)

	var err error
	maxRetries := 10

	for i := 1; i <= maxRetries; i++ {
		log.Printf("⏳ Attempting database connection (attempt %d/%d)...", i, maxRetries)

		config, err := pgx.ParseConfig(dsn)
		if err != nil {
			log.Fatalf("Failed to parse DB config: %v", err)
		}

		if sslmode == "disable" {
			config.DialFunc = func(ctx context.Context, network, addr string) (net.Conn, error) {
				dialer := net.Dialer{Timeout: 5 * time.Second}
				return dialer.DialContext(ctx, "tcp4", addr)
			}
		} else {
			config.DialFunc = func(ctx context.Context, network, addr string) (net.Conn, error) {
				dialer := net.Dialer{Timeout: 5 * time.Second}
				return dialer.DialContext(ctx, "tcp4", addr)
			}
		}

		sqlDB := stdlib.OpenDB(*config)

		DB, err = gorm.Open(postgres.New(postgres.Config{
			Conn: sqlDB,
		}), &gorm.Config{
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

	sqlDBInstance, err := DB.DB()
	if err != nil {
		log.Fatal("Failed to get generic database object:", err)
	}

	sqlDBInstance.SetMaxOpenConns(10)
	sqlDBInstance.SetMaxIdleConns(5)
	sqlDBInstance.SetConnMaxLifetime(time.Hour)

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