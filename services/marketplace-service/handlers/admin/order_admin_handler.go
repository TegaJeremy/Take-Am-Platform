package admin

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"marketplace-service/services"
	"marketplace-service/utils"
)

type OrderAdminHandler struct {
	orderService *services.OrderService
}

func NewOrderAdminHandler() *OrderAdminHandler {
	return &OrderAdminHandler{
		orderService: &services.OrderService{},
	}
}

func (h *OrderAdminHandler) UpdateDeliveryStatus(c *gin.Context) {
	orderID := c.Param("id")

	var input struct {
		DeliveryStatus string `json:"deliveryStatus" binding:"required,oneof=PENDING READY IN_TRANSIT DELIVERED PICKED_UP CANCELLED FAILED"`
	}

	if err := c.ShouldBindJSON(&input); err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, "Invalid input: "+err.Error(), nil)
		return
	}

	order, err := h.orderService.UpdateDeliveryStatus(orderID, input.DeliveryStatus)
	if err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, err.Error(), nil)
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "Delivery status updated successfully", order)
}

func (h *OrderAdminHandler) GetAllOrders(c *gin.Context) {
	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	limit, _ := strconv.Atoi(c.DefaultQuery("limit", "20"))

	orders, total, err := h.orderService.GetAllOrders(page, limit)
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
