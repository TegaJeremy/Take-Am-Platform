package services

import (
	"errors"

	"github.com/google/uuid"
	"gorm.io/gorm"
	"marketplace-service/database"
	"marketplace-service/models"
)

type OrderService struct{}

type CheckoutInput struct {
	DeliveryAddress string `json:"deliveryAddress" binding:"required"`
	DeliveryType    string `json:"deliveryType" binding:"required,oneof=PICKUP DELIVERY"`
}

func (s *OrderService) Checkout(buyerID string, input CheckoutInput) (*models.Order, error) {
	db := database.GetDB()

	buyerUUID, err := uuid.Parse(buyerID)
	if err != nil {
		return nil, errors.New("invalid buyer ID")
	}

	var cart models.Cart
	err = db.Preload("Items.Product").Where("buyer_id = ?", buyerUUID).First(&cart).Error
	if err == gorm.ErrRecordNotFound {
		return nil, errors.New("cart is empty")
	}
	if err != nil {
		return nil, err
	}

	if len(cart.Items) == 0 {
		return nil, errors.New("cart is empty")
	}

	var subtotal float64
	for _, item := range cart.Items {
		if item.Product.Status != "AVAILABLE" {
			return nil, errors.New("product " + item.Product.ProductName + " is no longer available")
		}

		if item.QuantityKg > item.Product.AvailableWeight {
			return nil, errors.New("insufficient stock for " + item.Product.ProductName)
		}

		subtotal += item.Subtotal
	}

	deliveryFee := 0.0
	if input.DeliveryType == "DELIVERY" {
		deliveryFee = 500.0
	}

	grandTotal := subtotal + deliveryFee

	tx := db.Begin()

	order := models.Order{
		BuyerID:         buyerUUID,
		Subtotal:        subtotal,
		DeliveryFee:     deliveryFee,
		GrandTotal:      grandTotal,
		DeliveryAddress: input.DeliveryAddress,
		DeliveryType:    input.DeliveryType,
		DeliveryStatus:  "PENDING",
		PaymentMethod:   "CARD",
		PaymentStatus:   "PENDING",
		Status:          "PENDING",
	}

	if err := tx.Create(&order).Error; err != nil {
		tx.Rollback()
		return nil, err
	}

	for _, cartItem := range cart.Items {
		orderItem := models.OrderItem{
			OrderID:     order.ID,
			ProductID:   cartItem.ProductID,
			ProductName: cartItem.Product.ProductName,
			Grade:       cartItem.Product.Grade,
			QuantityKg:  cartItem.QuantityKg,
			PricePerKg:  cartItem.PricePerKg,
			Subtotal:    cartItem.Subtotal,
		}

		if err := tx.Create(&orderItem).Error; err != nil {
			tx.Rollback()
			return nil, err
		}

		if err := tx.Model(&models.Product{}).
			Where("id = ?", cartItem.ProductID).
			Update("available_weight", gorm.Expr("available_weight - ?", cartItem.QuantityKg)).
			Error; err != nil {
			tx.Rollback()
			return nil, err
		}
	}

	if err := tx.Where("cart_id = ?", cart.ID).Delete(&models.CartItem{}).Error; err != nil {
		tx.Rollback()
		return nil, err
	}

	if err := tx.Commit().Error; err != nil {
		return nil, err
	}

	db.Preload("Items").First(&order, order.ID)

	return &order, nil
}

func (s *OrderService) GetUserOrders(buyerID string, page, limit int) ([]models.Order, int, error) {
	db := database.GetDB()

	buyerUUID, err := uuid.Parse(buyerID)
	if err != nil {
		return nil, 0, errors.New("invalid buyer ID")
	}

	var orders []models.Order
	var total int64

	db.Model(&models.Order{}).Where("buyer_id = ?", buyerUUID).Count(&total)

	offset := (page - 1) * limit

	err = db.Preload("Items").
		Where("buyer_id = ?", buyerUUID).
		Order("created_at DESC").
		Limit(limit).
		Offset(offset).
		Find(&orders).Error

	if err != nil {
		return nil, 0, err
	}

	return orders, int(total), nil
}

func (s *OrderService) GetOrderByID(buyerID, orderID string) (*models.Order, error) {
	db := database.GetDB()

	buyerUUID, err := uuid.Parse(buyerID)
	if err != nil {
		return nil, errors.New("invalid buyer ID")
	}

	var order models.Order
	err = db.Preload("Items").
		Where("id = ? AND buyer_id = ?", orderID, buyerUUID).
		First(&order).Error

	if err == gorm.ErrRecordNotFound {
		return nil, errors.New("order not found")
	}

	if err != nil {
		return nil, err
	}

	return &order, nil
}

func (s *OrderService) GetAllOrders(page, limit int) ([]models.Order, int, error) {
	db := database.GetDB()

	var orders []models.Order
	var total int64

	db.Model(&models.Order{}).Count(&total)

	offset := (page - 1) * limit

	err := db.Preload("Items").
		Order("created_at DESC").
		Limit(limit).
		Offset(offset).
		Find(&orders).Error

	if err != nil {
		return nil, 0, err
	}

	return orders, int(total), nil
}

func (s *OrderService) UpdateDeliveryStatus(orderID, status string) (*models.Order, error) {
	db := database.GetDB()

	validStatuses := []string{"PENDING", "READY", "IN_TRANSIT", "DELIVERED", "PICKED_UP", "CANCELLED", "FAILED"}
	isValid := false
	for _, validStatus := range validStatuses {
		if status == validStatus {
			isValid = true
			break
		}
	}

	if !isValid {
		return nil, errors.New("invalid delivery status")
	}

	var order models.Order
	if err := db.First(&order, "id = ?", orderID).Error; err != nil {
		return nil, errors.New("order not found")
	}

	order.DeliveryStatus = status
	if err := db.Save(&order).Error; err != nil {
		return nil, err
	}

	db.Preload("Items").First(&order, order.ID)

	return &order, nil
}