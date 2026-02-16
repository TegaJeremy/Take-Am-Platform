package routes

import (
	"github.com/gin-gonic/gin"
	"marketplace-service/config"
	"marketplace-service/handlers/admin"
	"marketplace-service/handlers/cart"
	"marketplace-service/handlers/order"
	"marketplace-service/handlers/payment"
	"marketplace-service/handlers/public"
	"marketplace-service/middleware"
)

func SetupRoutes(router *gin.Engine, cfg *config.Config) {

	router.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"success":  true,
			"message":  "Marketplace Service is running",
			"service":  "marketplace-service",
			"database": "connected",
		})
	})

	paymentHandler := payment.NewPaymentHandler(cfg.PaystackSecretKey)
	router.POST("/api/v1/marketplace/payment/webhook", paymentHandler.PaystackWebhook)

	v1 := router.Group("/api/v1")

	adminRoutes := v1.Group("/admin")
	adminRoutes.Use(middleware.AuthMiddleware())
	adminRoutes.Use(middleware.AdminMiddleware())
	{
		productHandler := admin.NewProductHandler()
		adminRoutes.POST("/products", productHandler.CreateProduct)
		adminRoutes.GET("/products", productHandler.GetAllProducts)
		adminRoutes.GET("/products/:id", productHandler.GetProduct)
		adminRoutes.PUT("/products/:id", productHandler.UpdateProduct)
		adminRoutes.DELETE("/products/:id", productHandler.DeleteProduct)

		orderAdminHandler := admin.NewOrderAdminHandler()
		adminRoutes.GET("/orders", orderAdminHandler.GetAllOrders)
		adminRoutes.PUT("/orders/:id/delivery-status", orderAdminHandler.UpdateDeliveryStatus)
	}

	marketplaceRoutes := v1.Group("/marketplace")

	publicProductHandler := public.NewPublicProductHandler()
	marketplaceRoutes.GET("/products", publicProductHandler.BrowseProducts)
	marketplaceRoutes.GET("/products/search", publicProductHandler.SearchProducts)
	marketplaceRoutes.GET("/products/filter", publicProductHandler.FilterProducts)
	marketplaceRoutes.GET("/products/:id", publicProductHandler.GetProductDetails)

	cartHandler := cart.NewCartHandler()
	cartRoutes := marketplaceRoutes.Group("/cart")
	cartRoutes.Use(middleware.AuthMiddleware())
	{
		cartRoutes.POST("/add", cartHandler.AddToCart)
		cartRoutes.GET("", cartHandler.GetCart)
		cartRoutes.PUT("/item/:id", cartHandler.UpdateCartItem)
		cartRoutes.DELETE("/item/:id", cartHandler.RemoveCartItem)
		cartRoutes.DELETE("", cartHandler.ClearCart)
	}

	orderHandler := order.NewOrderHandler()
	orderRoutes := marketplaceRoutes.Group("")
	orderRoutes.Use(middleware.AuthMiddleware())
	{
		orderRoutes.POST("/checkout", orderHandler.Checkout)
		orderRoutes.GET("/orders", orderHandler.GetUserOrders)
		orderRoutes.GET("/orders/:id", orderHandler.GetOrderDetails)
		orderRoutes.POST("/orders/:id/verify-pickup", orderHandler.VerifyPickup)
	}

	paymentRoutes := marketplaceRoutes.Group("/payment")
	paymentRoutes.Use(middleware.AuthMiddleware())
	{
		paymentRoutes.POST("/initialize/:orderId", paymentHandler.InitializePayment)
		paymentRoutes.GET("/verify/:reference", paymentHandler.VerifyPayment)
	}
}