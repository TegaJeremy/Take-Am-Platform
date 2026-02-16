package order

import (
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"gorm.io/gorm"
	"marketplace-service/database"
	"marketplace-service/models"
	"marketplace-service/services"
	"marketplace-service/utils"
)

type OrderHandler struct {
	orderService *services.OrderService
}

func NewOrderHandler() *OrderHandler {
	return &OrderHandler{
		orderService: &services.OrderService{},
	}
}

func (h *OrderHandler) Checkout(c *gin.Context) {
	var input services.CheckoutInput

	if err := c.ShouldBindJSON(&input); err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, "Invalid input: "+err.Error(), nil)
		return
	}

	buyerID, _ := c.Get("userId")

	order, err := h.orderService.Checkout(buyerID.(string), input)
	if err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, err.Error(), nil)
		return
	}

	utils.SuccessResponse(c, http.StatusCreated, "Order created successfully", order)
}

func (h *OrderHandler) GetUserOrders(c *gin.Context) {
	buyerID, _ := c.Get("userId")

	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	limit, _ := strconv.Atoi(c.DefaultQuery("limit", "10"))

	orders, total, err := h.orderService.GetUserOrders(buyerID.(string), page, limit)
	if err != nil {
		utils.ErrorResponse(c, http.StatusInternalServerError, "Failed to fetch orders", nil)
		return
	}

	response := map[string]interface{}{
		"orders": orders,
		"pagination": map[string]interface{}{
			"page":       page,
			"limit":      limit,
			"total":      total,
			"totalPages": (total + limit - 1) / limit,
		},
	}

	utils.SuccessResponse(c, http.StatusOK, "Orders fetched successfully", response)
}

func (h *OrderHandler) GetOrderDetails(c *gin.Context) {
	orderID := c.Param("id")
	buyerID, _ := c.Get("userId")

	order, err := h.orderService.GetOrderByID(buyerID.(string), orderID)
	if err != nil {
		utils.ErrorResponse(c, http.StatusNotFound, err.Error(), nil)
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "Order details fetched successfully", order)
}

func (h *OrderHandler) VerifyPickup(c *gin.Context) {
	orderID := c.Param("id")
	buyerID, _ := c.Get("userId")

	var input struct {
		PickupCode string `json:"pickupCode" binding:"required"`
	}

	if err := c.ShouldBindJSON(&input); err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, "Invalid input: "+err.Error(), nil)
		return
	}

	db := database.GetDB()

	var order models.Order
	err := db.Where("id = ? AND buyer_id = ?", orderID, buyerID).First(&order).Error
	if err == gorm.ErrRecordNotFound {
		utils.ErrorResponse(c, http.StatusNotFound, "Order not found", nil)
		return
	}
	if err != nil {
		utils.ErrorResponse(c, http.StatusInternalServerError, "Failed to fetch order", nil)
		return
	}

	if order.PaymentStatus != "PAID" {
		utils.ErrorResponse(c, http.StatusBadRequest, "Order not paid", nil)
		return
	}

	if order.DeliveryType != "PICKUP" {
		utils.ErrorResponse(c, http.StatusBadRequest, "This order is for delivery, not pickup", nil)
		return
	}

	if order.PickupCode != input.PickupCode {
		utils.ErrorResponse(c, http.StatusBadRequest, "Invalid pickup code", nil)
		return
	}

	if order.DeliveryStatus == "PICKED_UP" {
		utils.ErrorResponse(c, http.StatusBadRequest, "Order already picked up", nil)
		return
	}

	now := time.Now()
	order.DeliveryStatus = "PICKED_UP"
	order.PickedUpAt = &now

	if err := db.Save(&order).Error; err != nil {
		utils.ErrorResponse(c, http.StatusInternalServerError, "Failed to update order", nil)
		return
	}

	response := map[string]interface{}{
		"deliveryStatus": order.DeliveryStatus,
		"pickedUpAt":     order.PickedUpAt,
	}

	utils.SuccessResponse(c, http.StatusOK, "Order marked as picked up", response)
}