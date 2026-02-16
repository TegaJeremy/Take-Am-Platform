package utils

import (
	"github.com/gin-gonic/gin"
)

type Response struct {
	Success bool        `json:"success"`
	Message string      `json:"message"`
	Data    interface{} `json:"data,omitempty"`
	Error   string      `json:"error,omitempty"`
}

func SuccessResponse(c *gin.Context, statusCode int, message string, data interface{}) {
	c.JSON(statusCode, Response{
		Success: true,
		Message: message,
		Data:    data,
	})
}

func ErrorResponse(c *gin.Context, statusCode int, message string, err error) {
	response := Response{
		Success: false,
		Message: message,
	}

	// Only include technical error details in development
	// In production, log the error but don't expose it to users
	if err != nil {
		// Log the technical error
		// log.Printf("Error: %v", err)
		
		// Don't expose technical details to users
		// response.Error = err.Error()
	}

	c.JSON(statusCode, response)
}