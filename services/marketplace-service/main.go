package main

import (
	"log"
	
// 	"github.com/gin-contrib/cors"
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

	// ADD CORS MIDDLEWARE
// 	router.Use(cors.New(cors.Config{
// 		AllowOrigins: []string{
// 			"http://localhost:3000",
// 			"http://localhost:5173",
// 			"https://takeam.com",
// 			"https://www.takeam.com",
// 			"https://tegajeremy.github.io",  // ADD THIS
// 		},
// 		AllowMethods:     []string{"GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"},
// 		AllowHeaders:     []string{"Origin", "Content-Type", "Authorization", "X-User-Id", "X-User-Role", "X-User-Phone"},
// 		ExposeHeaders:    []string{"Content-Length"},
// 		AllowCredentials: true,
// 		MaxAge:           3600,
// 	}))

	routes.SetupRoutes(router, cfg)  

	log.Printf("Server running on port %s\n", cfg.Port)
	if err := router.Run(":" + cfg.Port); err != nil {
		log.Fatal("Failed to start server:", err)
	}
}