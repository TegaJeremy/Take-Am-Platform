package services

import (
	"errors"
	"log"
	"mime/multipart"
	"strings"
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
	"marketplace-service/database"
	"marketplace-service/models"
	"marketplace-service/utils"
)

type ProductService struct{}

// ============================================================================
// CREATE PRODUCT
// ============================================================================

// CreateProduct creates a new product with full validation
func (s *ProductService) CreateProduct(input CreateProductInput, adminId string) (*models.Product, error) {
	if err := s.validateProductInput(input); err != nil {
		return nil, err
	}

	adminUUID, err := uuid.Parse(adminId)
	if err != nil {
		return nil, errors.New("invalid admin ID")
	}

	product := s.buildProductFromInput(input, adminUUID)
	
	if err := s.setOptionalFields(&product, input); err != nil {
		return nil, err
	}

	if err := s.saveProduct(&product); err != nil {
		return nil, err
	}

	return &product, nil
}

// validateProductInput validates all input fields
func (s *ProductService) validateProductInput(input CreateProductInput) error {
	validators := []struct {
		fn  func() error
		msg string
	}{
		{func() error { return utils.ValidateProductName(input.ProductName) }, "product name"},
		{func() error { return utils.ValidateGrade(input.Grade) }, "grade"},
		{func() error { return utils.ValidateWeight(input.AvailableWeight) }, "weight"},
		{func() error { return utils.ValidatePrice(input.PricePerKg) }, "price"},
		{func() error { return utils.ValidateLocation(input.Location) }, "location"},
		{func() error { return utils.ValidateDescription(input.Description) }, "description"},
		{func() error { return utils.ValidateUUID(input.GradingID) }, "grading ID"},
		{func() error { return utils.ValidateUUID(input.TraderID) }, "trader ID"},
	}

	for _, v := range validators {
		if err := v.fn(); err != nil {
			return err
		}
	}

	return nil
}

// buildProductFromInput constructs product model from input
func (s *ProductService) buildProductFromInput(input CreateProductInput, adminUUID uuid.UUID) models.Product {
	return models.Product{
		ProductName:     strings.TrimSpace(input.ProductName),
		Grade:           s.normalizeGrade(input.Grade),
		AvailableWeight: input.AvailableWeight,
		PricePerKg:      input.PricePerKg,
		Description:     strings.TrimSpace(input.Description),
		Location:        strings.TrimSpace(input.Location),
		Status:          "AVAILABLE",
		CreatedByAdmin:  adminUUID,
	}
}

// normalizeGrade converts "A" to "Grade A" format
func (s *ProductService) normalizeGrade(grade string) string {
	grade = strings.TrimSpace(grade)
	gradeUpper := strings.ToUpper(grade)
	
	if len(gradeUpper) == 1 && (gradeUpper == "A" || gradeUpper == "B" || gradeUpper == "C" || gradeUpper == "D") {
		return "Grade " + gradeUpper
	}
	
	return grade
}

// setOptionalFields sets optional UUID and timestamp fields
func (s *ProductService) setOptionalFields(product *models.Product, input CreateProductInput) error {
	if input.GradingID != "" {
		if gradingUUID, err := uuid.Parse(input.GradingID); err == nil {
			product.GradingID = &gradingUUID
		}
	}

	if input.TraderID != "" {
		if traderUUID, err := uuid.Parse(input.TraderID); err == nil {
			product.TraderID = &traderUUID
		}
	}

	if input.ExpiresAt != "" {
		if expiresAt, err := time.Parse(time.RFC3339, input.ExpiresAt); err == nil {
			product.ExpiresAt = &expiresAt
		}
	}

	return nil
}

// saveProduct persists product to database
func (s *ProductService) saveProduct(product *models.Product) error {
	db := database.GetDB()
	return db.Create(product).Error
}

// ============================================================================
// IMAGE UPLOAD
// ============================================================================

// UploadProductImages handles image uploads with validation
func (s *ProductService) UploadProductImages(productID string, files []*multipart.FileHeader, adminID string) error {
	if err := utils.ValidateImageCount(len(files)); err != nil {
		return err
	}

	for i, fileHeader := range files {
		if err := s.uploadSingleImage(productID, fileHeader, i, adminID); err != nil {
			log.Printf("Failed to upload image %d: %v", i, err)
		}
	}

	return nil
}

// uploadSingleImage validates and uploads a single image
func (s *ProductService) uploadSingleImage(productID string, fileHeader *multipart.FileHeader, index int, adminID string) error {
	if err := utils.ValidateImageFile(fileHeader.Filename, fileHeader.Size); err != nil {
		return err
	}

	file, err := fileHeader.Open()
	if err != nil {
		return err
	}
	defer file.Close()

	imageURL, err := cloudinaryService.UploadImage(file, fileHeader.Filename)
	if err != nil {
		return err
	}

	isPrimary := index == 0
	return s.saveProductImage(productID, imageURL, isPrimary, index, adminID)
}

// saveProductImage saves image metadata to database
func (s *ProductService) saveProductImage(productID, imageURL string, isPrimary bool, displayOrder int, uploadedBy string) error {
	uploaderUUID, err := uuid.Parse(uploadedBy)
	if err != nil {
		return err
	}

	image := models.ProductImage{
		ProductID:    uuid.MustParse(productID),
		ImageURL:     imageURL,
		IsPrimary:    isPrimary,
		DisplayOrder: displayOrder,
		UploadedBy:   uploaderUUID,
	}

	db := database.GetDB()
	return db.Create(&image).Error
}

// ============================================================================
// UPDATE PRODUCT
// ============================================================================

// UpdateProduct updates an existing product with validation
func (s *ProductService) UpdateProduct(id string, input UpdateProductInput) (*models.Product, error) {
	product, err := s.findProductByID(id)
	if err != nil {
		return nil, err
	}

	if err := s.applyUpdates(product, input); err != nil {
		return nil, err
	}

	if err := s.saveProductUpdates(product); err != nil {
		return nil, err
	}

	return product, nil
}

// findProductByID retrieves product from database
func (s *ProductService) findProductByID(id string) (*models.Product, error) {
	var product models.Product
	db := database.GetDB()

	if err := db.First(&product, "id = ?", id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, errors.New("product not found")
		}
		return nil, err
	}

	return &product, nil
}

// applyUpdates applies validated updates to product
func (s *ProductService) applyUpdates(product *models.Product, input UpdateProductInput) error {
	updates := []func(*models.Product, UpdateProductInput) error{
		s.updateProductName,
		s.updateGrade,
		s.updateWeight,
		s.updatePrice,
		s.updateDescription,
		s.updateLocation,
		s.updateStatus,
	}

	for _, update := range updates {
		if err := update(product, input); err != nil {
			return err
		}
	}

	return nil
}

// updateProductName updates product name if provided
func (s *ProductService) updateProductName(product *models.Product, input UpdateProductInput) error {
	if input.ProductName != nil {
		if err := utils.ValidateProductName(*input.ProductName); err != nil {
			return err
		}
		product.ProductName = strings.TrimSpace(*input.ProductName)
	}
	return nil
}

// updateGrade updates grade if provided
func (s *ProductService) updateGrade(product *models.Product, input UpdateProductInput) error {
	if input.Grade != nil {
		if err := utils.ValidateGrade(*input.Grade); err != nil {
			return err
		}
		product.Grade = s.normalizeGrade(*input.Grade)
	}
	return nil
}

// updateWeight updates available weight if provided
func (s *ProductService) updateWeight(product *models.Product, input UpdateProductInput) error {
	if input.AvailableWeight != nil {
		if err := utils.ValidateWeight(*input.AvailableWeight); err != nil {
			return err
		}
		product.AvailableWeight = *input.AvailableWeight
	}
	return nil
}

// updatePrice updates price per kg if provided
func (s *ProductService) updatePrice(product *models.Product, input UpdateProductInput) error {
	if input.PricePerKg != nil {
		if err := utils.ValidatePrice(*input.PricePerKg); err != nil {
			return err
		}
		product.PricePerKg = *input.PricePerKg
	}
	return nil
}

// updateDescription updates description if provided
func (s *ProductService) updateDescription(product *models.Product, input UpdateProductInput) error {
	if input.Description != nil {
		if err := utils.ValidateDescription(*input.Description); err != nil {
			return err
		}
		product.Description = strings.TrimSpace(*input.Description)
	}
	return nil
}

// updateLocation updates location if provided
func (s *ProductService) updateLocation(product *models.Product, input UpdateProductInput) error {
	if input.Location != nil {
		if err := utils.ValidateLocation(*input.Location); err != nil {
			return err
		}
		product.Location = strings.TrimSpace(*input.Location)
	}
	return nil
}

// updateStatus updates status if provided
func (s *ProductService) updateStatus(product *models.Product, input UpdateProductInput) error {
	if input.Status != nil {
		if err := utils.ValidateStatus(*input.Status); err != nil {
			return err
		}
		product.Status = strings.ToUpper(*input.Status)
	}
	return nil
}

// saveProductUpdates persists updated product
func (s *ProductService) saveProductUpdates(product *models.Product) error {
	db := database.GetDB()
	return db.Save(product).Error
}

// ============================================================================
// READ & DELETE OPERATIONS
// ============================================================================

// GetAllProducts returns all products with images
func (s *ProductService) GetAllProducts() ([]models.Product, error) {
	var products []models.Product
	db := database.GetDB()

	if err := db.Preload("Images").Find(&products).Error; err != nil {
		return nil, err
	}

	return products, nil
}

// GetProductByID returns a single product with images
func (s *ProductService) GetProductByID(id string) (*models.Product, error) {
	var product models.Product
	db := database.GetDB()

	if err := db.Preload("Images").First(&product, "id = ?", id).Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, errors.New("product not found")
		}
		return nil, err
	}

	return &product, nil
}

// DeleteProduct soft deletes a product
func (s *ProductService) DeleteProduct(id string) error {
	db := database.GetDB()

	result := db.Delete(&models.Product{}, "id = ?", id)
	if result.Error != nil {
		return result.Error
	}

	if result.RowsAffected == 0 {
		return errors.New("product not found")
	}

	return nil
}

// ============================================================================
// INPUT DTOs
// ============================================================================

type CreateProductInput struct {
	ProductName     string  `json:"productName" binding:"required"`
	Grade           string  `json:"grade" binding:"required"`
	AvailableWeight float64 `json:"availableWeight" binding:"required,gt=0"`
	PricePerKg      float64 `json:"pricePerKg" binding:"required,gt=0"`
	Description     string  `json:"description"`
	Location        string  `json:"location"`
	GradingID       string  `json:"gradingId"`
	TraderID        string  `json:"traderId"`
	ExpiresAt       string  `json:"expiresAt"`
}

type UpdateProductInput struct {
	ProductName     *string  `json:"productName"`
	Grade           *string  `json:"grade"`
	AvailableWeight *float64 `json:"availableWeight"`
	PricePerKg      *float64 `json:"pricePerKg"`
	Description     *string  `json:"description"`
	Location        *string  `json:"location"`
	Status          *string  `json:"status"`
}







// ============================================================================
// PUBLIC BROWSE METHODS
// ============================================================================

// GetPublicProducts returns paginated available products for public viewing
func (s *ProductService) GetPublicProducts(page, limit int) ([]models.Product, int, error) {
	var products []models.Product
	var total int64

	db := database.GetDB()

	// Count total available products
	if err := db.Model(&models.Product{}).Where("status = ?", "AVAILABLE").Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// Calculate offset
	offset := (page - 1) * limit

	// Fetch products with images
	err := db.Preload("Images").
		Where("status = ?", "AVAILABLE").
		Order("created_at DESC").
		Limit(limit).
		Offset(offset).
		Find(&products).Error

	if err != nil {
		return nil, 0, err
	}

	return products, int(total), nil
}

// SearchProducts searches products by name or description
func (s *ProductService) SearchProducts(query string, page, limit int) ([]models.Product, int, error) {
	var products []models.Product
	var total int64

	db := database.GetDB()

	searchPattern := "%" + strings.ToLower(query) + "%"

	// Count matching products
	countQuery := db.Model(&models.Product{}).
		Where("status = ? AND (LOWER(product_name) LIKE ? OR LOWER(description) LIKE ?)", 
			"AVAILABLE", searchPattern, searchPattern)

	if err := countQuery.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// Calculate offset
	offset := (page - 1) * limit

	// Search products
	err := db.Preload("Images").
		Where("status = ? AND (LOWER(product_name) LIKE ? OR LOWER(description) LIKE ?)", 
			"AVAILABLE", searchPattern, searchPattern).
		Order("created_at DESC").
		Limit(limit).
		Offset(offset).
		Find(&products).Error

	if err != nil {
		return nil, 0, err
	}

	return products, int(total), nil
}

// ProductFilters holds filter criteria
type ProductFilters struct {
	Grade    string
	Location string
	MinPrice *float64
	MaxPrice *float64
}

// FilterProducts filters products based on criteria
func (s *ProductService) FilterProducts(filters ProductFilters, page, limit int) ([]models.Product, int, error) {
	var products []models.Product
	var total int64

	db := database.GetDB()

	query := db.Model(&models.Product{}).Where("status = ?", "AVAILABLE")

	// Apply filters
	if filters.Grade != "" {
		grade := strings.TrimSpace(filters.Grade)
		gradeUpper := strings.ToUpper(grade)
		
		// Handle both "A" and "Grade A" formats
		if len(gradeUpper) == 1 && (gradeUpper == "A" || gradeUpper == "B" || gradeUpper == "C" || gradeUpper == "D") {
			query = query.Where("grade = ? OR grade = ?", gradeUpper, "Grade "+gradeUpper)
		} else {
			query = query.Where("LOWER(grade) = ?", strings.ToLower(grade))
		}
	}

	if filters.Location != "" {
		query = query.Where("LOWER(location) LIKE ?", "%"+strings.ToLower(filters.Location)+"%")
	}

	if filters.MinPrice != nil {
		query = query.Where("price_per_kg >= ?", *filters.MinPrice)
	}

	if filters.MaxPrice != nil {
		query = query.Where("price_per_kg <= ?", *filters.MaxPrice)
	}

	// Count matching products
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// Calculate offset
	offset := (page - 1) * limit

	// Fetch filtered products
	err := query.Preload("Images").
		Order("created_at DESC").
		Limit(limit).
		Offset(offset).
		Find(&products).Error

	if err != nil {
		return nil, 0, err
	}

	return products, int(total), nil
}