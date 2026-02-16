package payment

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"marketplace-service/database"
	"marketplace-service/models"
	"marketplace-service/services"
	"marketplace-service/utils"
)

type PaymentHandler struct {
	paymentService *services.PaymentService
	secretKey      string  // ADD THIS
}

func NewPaymentHandler(paystackSecretKey string) *PaymentHandler {
	return &PaymentHandler{
		paymentService: services.NewPaymentService(paystackSecretKey),
		secretKey:      paystackSecretKey,  // STORE IT
	}
}


func (h *PaymentHandler) InitializePayment(c *gin.Context) {
	orderID := c.Param("orderId")
	buyerID, _ := c.Get("userId")
	buyerPhone, exists := c.Get("userPhone")

	
	var input struct {
		CallbackURL string `json:"callbackUrl"`
	}
	c.ShouldBindJSON(&input)

	db := database.GetDB()

	var order models.Order
	if err := db.Where("id = ? AND buyer_id = ?", orderID, buyerID).First(&order).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			utils.ErrorResponse(c, http.StatusNotFound, "Order not found", nil)
			return
		}
		utils.ErrorResponse(c, http.StatusInternalServerError, "Failed to fetch order", nil)
		return
	}

	if order.PaymentStatus == "PAID" {
		utils.ErrorResponse(c, http.StatusBadRequest, "Order already paid", nil)
		return
	}

	var buyerEmail string
	if exists && buyerPhone != nil {
		buyerEmail = buyerPhone.(string) + "@takeam.com"
	} else {
		buyerEmail = "buyer@takeam.com"
	}

	
	callbackURL := input.CallbackURL
	if callbackURL == "" {
		callbackURL = "takeam://payment/callback"  
	}

	resp, err := h.paymentService.InitializePayment(orderID, buyerEmail, callbackURL)
	if err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, err.Error(), nil)
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "Payment initialized successfully", resp)
}

func (h *PaymentHandler) VerifyPayment(c *gin.Context) {
	reference := c.Param("reference")

	if err := h.paymentService.VerifyPayment(reference); err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, err.Error(), nil)
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "Payment verified successfully", nil)
}