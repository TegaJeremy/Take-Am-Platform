package services

import (
	"errors"
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
	"marketplace-service/database"
	"marketplace-service/models"
)

type CartService struct{}

type AddToCartInput struct {
	ProductID  string  `json:"productId" binding:"required"`
	QuantityKg float64 `json:"quantityKg" binding:"required,gt=0"`
}

func (s *CartService) AddToCart(buyerID string, input AddToCartInput) (*models.Cart, error) {
	db := database.GetDB()

	var product models.Product
	if err := db.First(&product, "id = ? AND status = ?", input.ProductID, "AVAILABLE").Error; err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, errors.New("product not found or not available")
		}
		return nil, err
	}

	if input.QuantityKg > product.AvailableWeight {
		return nil, errors.New("requested quantity exceeds available stock")
	}

	cart, err := s.getOrCreateCart(buyerID)
	if err != nil {
		return nil, err
	}

	var existingItem models.CartItem
	err = db.Where("cart_id = ? AND product_id = ?", cart.ID, product.ID).First(&existingItem).Error

	if err == nil {
		newQuantity := existingItem.QuantityKg + input.QuantityKg

		if newQuantity > product.AvailableWeight {
			return nil, errors.New("total quantity exceeds available stock")
		}

		existingItem.QuantityKg = newQuantity
		existingItem.Subtotal = newQuantity * existingItem.PricePerKg

		if err := db.Save(&existingItem).Error; err != nil {
			return nil, err
		}
	} else if err == gorm.ErrRecordNotFound {
		cartItem := models.CartItem{
			CartID:     cart.ID,
			ProductID:  product.ID,
			QuantityKg: input.QuantityKg,
			PricePerKg: product.PricePerKg,
			Subtotal:   input.QuantityKg * product.PricePerKg,
		}

		if err := db.Create(&cartItem).Error; err != nil {
			return nil, err
		}
	} else {
		return nil, err
	}

	return s.GetCart(buyerID)
}

func (s *CartService) GetCart(buyerID string) (*models.Cart, error) {
	db := database.GetDB()

	buyerUUID, err := uuid.Parse(buyerID)
	if err != nil {
		return nil, errors.New("invalid buyer ID")
	}

	var cart models.Cart
	err = db.Preload("Items.Product.Images").
		Where("buyer_id = ?", buyerUUID).
		First(&cart).Error

	if err == gorm.ErrRecordNotFound {
		return &models.Cart{
			BuyerID: buyerUUID,
			Items:   []models.CartItem{},
		}, nil
	}

	if err != nil {
		return nil, err
	}

	return &cart, nil
}

func (s *CartService) UpdateCartItem(buyerID, itemID string, quantityKg float64) (*models.Cart, error) {
	db := database.GetDB()

	if quantityKg <= 0 {
		return nil, errors.New("quantity must be greater than 0")
	}

	cart, err := s.getOrCreateCart(buyerID)
	if err != nil {
		return nil, err
	}

	var cartItem models.CartItem
	err = db.Preload("Product").
		Where("id = ? AND cart_id = ?", itemID, cart.ID).
		First(&cartItem).Error

	if err == gorm.ErrRecordNotFound {
		return nil, errors.New("cart item not found")
	}
	if err != nil {
		return nil, err
	}

	if quantityKg > cartItem.Product.AvailableWeight {
		return nil, errors.New("requested quantity exceeds available stock")
	}

	cartItem.QuantityKg = quantityKg
	cartItem.Subtotal = quantityKg * cartItem.PricePerKg

	if err := db.Save(&cartItem).Error; err != nil {
		return nil, err
	}

	return s.GetCart(buyerID)
}

func (s *CartService) RemoveCartItem(buyerID, itemID string) (*models.Cart, error) {
	db := database.GetDB()

	cart, err := s.getOrCreateCart(buyerID)
	if err != nil {
		return nil, err
	}

	result := db.Where("id = ? AND cart_id = ?", itemID, cart.ID).Delete(&models.CartItem{})

	if result.Error != nil {
		return nil, result.Error
	}

	if result.RowsAffected == 0 {
		return nil, errors.New("cart item not found")
	}

	return s.GetCart(buyerID)
}

func (s *CartService) ClearCart(buyerID string) error {
	db := database.GetDB()

	buyerUUID, err := uuid.Parse(buyerID)
	if err != nil {
		return errors.New("invalid buyer ID")
	}

	var cart models.Cart
	err = db.Where("buyer_id = ?", buyerUUID).First(&cart).Error

	if err == gorm.ErrRecordNotFound {
		return nil
	}
	if err != nil {
		return err
	}

	return db.Where("cart_id = ?", cart.ID).Delete(&models.CartItem{}).Error
}

func (s *CartService) getOrCreateCart(buyerID string) (*models.Cart, error) {
	db := database.GetDB()

	buyerUUID, err := uuid.Parse(buyerID)
	if err != nil {
		return nil, errors.New("invalid buyer ID")
	}

	var cart models.Cart
	err = db.Where("buyer_id = ?", buyerUUID).First(&cart).Error

	if err == gorm.ErrRecordNotFound {
		cart = models.Cart{
			BuyerID:   buyerUUID,
			CreatedAt: time.Now(),
			UpdatedAt: time.Now(),
		}

		if err := db.Create(&cart).Error; err != nil {
			return nil, err
		}
	} else if err != nil {
		return nil, err
	}

	return &cart, nil
}