package admin

import (
	"log"
	"net/http"
	"strconv"
	"strings"

	"github.com/gin-gonic/gin"
	"marketplace-service/services"
	"marketplace-service/utils"
)

type ProductHandler struct {
	productService *services.ProductService
}

func NewProductHandler() *ProductHandler {
	return &ProductHandler{
		productService: &services.ProductService{},
	}
}


func (h *ProductHandler) CreateProduct(c *gin.Context) {
	if err := c.Request.ParseMultipartForm(10 << 20); err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, "Failed to parse form. Maximum file size is 10MB.", err)
		return
	}

	input, err := h.parseProductInput(c)
	if err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, "Invalid input: "+err.Error(), nil)
		return
	}

	adminId, _ := c.Get("userId")

	product, err := h.productService.CreateProduct(input, adminId.(string))
	if err != nil {
		errMsg := h.formatErrorMessage(err)
		utils.ErrorResponse(c, http.StatusBadRequest, errMsg, nil)
		return
	}

	form, _ := c.MultipartForm()
	files := form.File["images"]

	if len(files) > 0 {
		if err := h.productService.UploadProductImages(product.ID.String(), files, adminId.(string)); err != nil {
			log.Printf("Image upload warning: %v", err)
		}

		product, _ = h.productService.GetProductByID(product.ID.String())
	}

	utils.SuccessResponse(c, http.StatusCreated, "Product created successfully", product)
}


func (h *ProductHandler) GetAllProducts(c *gin.Context) {
	products, err := h.productService.GetAllProducts()
	if err != nil {
		utils.ErrorResponse(c, http.StatusInternalServerError, "Failed to fetch products", nil)
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "Products fetched successfully", products)
}


func (h *ProductHandler) GetProduct(c *gin.Context) {
	id := c.Param("id")

	product, err := h.productService.GetProductByID(id)
	if err != nil {
		utils.ErrorResponse(c, http.StatusNotFound, "Product not found", nil)
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "Product fetched successfully", product)
}


func (h *ProductHandler) UpdateProduct(c *gin.Context) {
	id := c.Param("id")
	var input services.UpdateProductInput

	if err := c.ShouldBindJSON(&input); err != nil {
		utils.ErrorResponse(c, http.StatusBadRequest, "Invalid input: "+err.Error(), nil)
		return
	}

	product, err := h.productService.UpdateProduct(id, input)
	if err != nil {
		errMsg := h.formatErrorMessage(err)
		utils.ErrorResponse(c, http.StatusBadRequest, errMsg, nil)
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "Product updated successfully", product)
}


func (h *ProductHandler) DeleteProduct(c *gin.Context) {
	id := c.Param("id")

	err := h.productService.DeleteProduct(id)
	if err != nil {
		utils.ErrorResponse(c, http.StatusNotFound, "Product not found or already deleted", nil)
		return
	}

	utils.SuccessResponse(c, http.StatusOK, "Product deleted successfully", nil)
}


func (h *ProductHandler) formatErrorMessage(err error) string {
	errStr := err.Error()

	if strings.Contains(errStr, "value too long") {
		if strings.Contains(errStr, "character(1)") {
			return "Database schema error detected. Please run database migration or contact support."
		}
		return "One or more fields exceed maximum allowed length"
	}

	if strings.Contains(errStr, "violates foreign key constraint") {
		return "Invalid reference ID. Please verify grading ID and trader ID."
	}

	if strings.Contains(errStr, "duplicate key") {
		return "A product with this information already exists"
	}

	if strings.Contains(errStr, "violates not-null constraint") {
		return "A required field is missing. Please check your input."
	}

	if strings.Contains(errStr, "grade must be") ||
		strings.Contains(errStr, "must be") ||
		strings.Contains(errStr, "is required") ||
		strings.Contains(errStr, "invalid") {
		return errStr
	}

	if strings.Contains(errStr, "SQLSTATE") || strings.Contains(errStr, "ERROR:") {
		return "Database error occurred. Please verify your input and try again."
	}

	return errStr
}


func (h *ProductHandler) parseProductInput(c *gin.Context) (services.CreateProductInput, error) {
	weight, err := strconv.ParseFloat(c.PostForm("availableWeight"), 64)
	if err != nil {
		return services.CreateProductInput{}, err
	}

	price, err := strconv.ParseFloat(c.PostForm("pricePerKg"), 64)
	if err != nil {
		return services.CreateProductInput{}, err
	}

	return services.CreateProductInput{
		ProductName:     c.PostForm("productName"),
		Grade:           c.PostForm("grade"),
		AvailableWeight: weight,
		PricePerKg:      price,
		Description:     c.PostForm("description"),
		Location:        c.PostForm("location"),
		GradingID:       c.PostForm("gradingId"),
		TraderID:        c.PostForm("traderId"),
		ExpiresAt:       c.PostForm("expiresAt"),
	}, nil
}