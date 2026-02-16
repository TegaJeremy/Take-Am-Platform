package middleware

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"marketplace-service/utils"
)

// AuthMiddleware extracts user info from gateway headers
func AuthMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		// Get headers set by API Gateway
		userId := c.GetHeader("X-User-Id")
		userRole := c.GetHeader("X-User-Role")
		userPhone := c.GetHeader("X-User-Phone")

		if userId == "" {
			utils.ErrorResponse(c, http.StatusUnauthorized, "Authentication required", nil)
			c.Abort()
			return
		}

		// Store user info in context
		c.Set("userId", userId)
		c.Set("role", userRole)
		c.Set("phoneNumber", userPhone)

		c.Next()
	}
}

// AdminMiddleware checks if user is admin or super admin
func AdminMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		role, exists := c.Get("role")

		if !exists {
			utils.ErrorResponse(c, http.StatusForbidden, "Role not found", nil)
			c.Abort()
			return
		}

		roleStr := role.(string)

		// Check if user is ADMIN or SUPER_ADMIN
		if roleStr != "ADMIN" && roleStr != "SUPER_ADMIN" {
			utils.ErrorResponse(c, http.StatusForbidden, "Admin or Super Admin access required", nil)
			c.Abort()
			return
		}

		c.Next()
	}
}