package cart

import (
	"net/http"

	"marketplace-service/models"
	"marketplace-service/services"
	"marketplace-service/utils"

	"github.com/gin-gonic/gin"
)

type CartHandler struct {
	cartService *services.CartService
}

func NewCartHandler() *CartHandler {
	return &CartHandler{
		cartService: &services.CartService{},
	}
}


func (h *CartHandler) AddToCart(c *gin.Context) {
	var input services.AddToCartInput

	if err := c.ShouldBindJSON(&input); err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, "Invalid input: "+err.Error(), nil)
		return
	}

	buyerID, exists := c.Get("userId")
	if !exists {
		utils.ErrorResponse(c, http.StatusUnauthorized, "User not authenticated", nil)
		return
	}

	cart, err := h.cartService.AddToCart(buyerID.(string), input)
	if err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, err.Error(), nil)
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "Product added to cart successfully", cart)
}


func (h *CartHandler) GetCart(c *gin.Context) {
	buyerID, exists := c.Get("userId")
	if !exists {
		utils.ErrorResponse(c, http.StatusUnauthorized, "User not authenticated", nil)
		return
	}

	cart, err := h.cartService.GetCart(buyerID.(string))
	if err != nil {
		utils.ErrorResponse(c, http.StatusInternalServerError, "Failed to fetch cart", nil)
		return
	}

	total := h.calculateTotal(cart.Items)

	response := map[string]interface{}{
		"cart":       cart,
		"totalItems": len(cart.Items),
		"total":      total,
	}

	utils.SuccessResponse(c, http.StatusOK, "Cart fetched successfully", response)
}


func (h *CartHandler) UpdateCartItem(c *gin.Context) {
	itemID := c.Param("id")

	var input struct {
		QuantityKg float64 `json:"quantityKg" binding:"required,gt=0"`
	}

	if err := c.ShouldBindJSON(&input); err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, "Invalid input: "+err.Error(), nil)
		return
	}

	buyerID, _ := c.Get("userId")

	cart, err := h.cartService.UpdateCartItem(buyerID.(string), itemID, input.QuantityKg)
	if err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, err.Error(), nil)
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "Cart item updated successfully", cart)
}


func (h *CartHandler) RemoveCartItem(c *gin.Context) {
	itemID := c.Param("id")
	buyerID, _ := c.Get("userId")

	cart, err := h.cartService.RemoveCartItem(buyerID.(string), itemID)
	if err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, err.Error(), nil)
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "Item removed from cart successfully", cart)
}


func (h *CartHandler) ClearCart(c *gin.Context) {
	buyerID, _ := c.Get("userId")

	err := h.cartService.ClearCart(buyerID.(string))
	if err != nil {
		utils.ErrorResponse(c, http.StatusInternalServerError, "Failed to clear cart", nil)
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "Cart cleared successfully", nil)
}


func (h *CartHandler) calculateTotal(items []models.CartItem) float64 {
	var total float64
	for _, item := range items {
		total += item.Subtotal
	}
	return total
}