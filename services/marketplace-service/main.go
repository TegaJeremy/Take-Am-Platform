package main

import (
	"log"

	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
	"marketplace-service/config"
	"marketplace-service/database"
	"marketplace-service/routes"
	"marketplace-service/services"
)

func main() {
	_ = godotenv.Load()
	log.Println("Starting Marketplace Service...")

	cfg := config.LoadConfig()

	database.ConnectDB()

	if err := services.InitCloudinary(cfg.CloudinaryName, cfg.CloudinaryAPIKey, cfg.CloudinarySecret); err != nil {
		log.Fatal("Failed to initialize Cloudinary:", err)
	}

	router := gin.Default()

	routes.SetupRoutes(router, cfg)  

	log.Printf("Server running on port %s\n", cfg.Port)
	if err := router.Run(":" + cfg.Port); err != nil {
		log.Fatal("Failed to start server:", err)
	}
}