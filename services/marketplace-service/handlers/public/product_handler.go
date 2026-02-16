package public

import (
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"marketplace-service/services"
	"marketplace-service/utils"
)

type PublicProductHandler struct {
	productService *services.ProductService
}

func NewPublicProductHandler() *PublicProductHandler {
	return &PublicProductHandler{
		productService: &services.ProductService{},
	}
}


func (h *PublicProductHandler) BrowseProducts(c *gin.Context) {
	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	limit, _ := strconv.Atoi(c.DefaultQuery("limit", "20"))

	if page < 1 {
		page = 1
	}
	if limit < 1 || limit > 100 {
		limit = 20
	}

	products, total, err := h.productService.GetPublicProducts(page, limit)
	if err != nil {
		utils.ErrorResponse(c, http.StatusInternalServerError, "Failed to fetch products", nil)
		return
	}

	response := map[string]interface{}{
		"products": products,
		"pagination": map[string]interface{}{
			"page":       page,
			"limit":      limit,
			"total":      total,
			"totalPages": (total + limit - 1) / limit,
		},
	}

	utils.SuccessResponse(c, http.StatusOK, "Products fetched successfully", response)
}


func (h *PublicProductHandler) GetProductDetails(c *gin.Context) {
	id := c.Param("id")

	product, err := h.productService.GetProductByID(id)
	if err != nil {
		utils.ErrorResponse(c, http.StatusNotFound, "Product not found", nil)
		return
	}

	if product.Status != "AVAILABLE" {
		utils.ErrorResponse(c, http.StatusNotFound, "Product not available", nil)
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "Product details fetched successfully", product)
}


func (h *PublicProductHandler) SearchProducts(c *gin.Context) {
	query := strings.TrimSpace(c.Query("query"))
	
	if query == "" {
		utils.ErrorResponse(c, http.StatusBadRequest, "Search query is required", nil)
		return
	}

	if len(query) < 2 {
		utils.ErrorResponse(c, http.StatusBadRequest, "Search query must be at least 2 characters", nil)
		return
	}

	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	limit, _ := strconv.Atoi(c.DefaultQuery("limit", "20"))

	products, total, err := h.productService.SearchProducts(query, page, limit)
	if err != nil {
		utils.ErrorResponse(c, http.StatusInternalServerError, "Search failed", nil)
		return
	}

	response := map[string]interface{}{
		"products": products,
		"query":    query,
		"pagination": map[string]interface{}{
			"page":       page,
			"limit":      limit,
			"total":      total,
			"totalPages": (total + limit - 1) / limit,
		},
	}

	utils.SuccessResponse(c, http.StatusOK, "Search results fetched successfully", response)
}


func (h *PublicProductHandler) FilterProducts(c *gin.Context) {
	filters := services.ProductFilters{
		Grade:    c.Query("grade"),
		Location: c.Query("location"),
	}

	if minPrice := c.Query("minPrice"); minPrice != "" {
		if price, err := strconv.ParseFloat(minPrice, 64); err == nil {
			filters.MinPrice = &price
		}
	}

	if maxPrice := c.Query("maxPrice"); maxPrice != "" {
		if price, err := strconv.ParseFloat(maxPrice, 64); err == nil {
			filters.MaxPrice = &price
		}
	}

	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	limit, _ := strconv.Atoi(c.DefaultQuery("limit", "20"))

	products, total, err := h.productService.FilterProducts(filters, page, limit)
	if err != nil {
		utils.ErrorResponse(c, http.StatusInternalServerError, "Failed to filter products", nil)
		return
	}

	response := map[string]interface{}{
		"products": products,
		"filters":  filters,
		"pagination": map[string]interface{}{
			"page":       page,
			"limit":      limit,
			"total":      total,
			"totalPages": (total + limit - 1) / limit,
		},
	}

	utils.SuccessResponse(c, http.StatusOK, "Filtered products fetched successfully", response)
}
