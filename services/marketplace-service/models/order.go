package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// Order represents a buyer's order
type Order struct {
	ID               uuid.UUID   `json:"id" gorm:"type:uuid;primary_key"`
	OrderNumber      string      `json:"orderNumber" gorm:"type:varchar(50);unique;not null"`
	BuyerID          uuid.UUID   `json:"buyerId" gorm:"type:uuid;not null"`
	Subtotal         float64     `json:"subtotal" gorm:"type:decimal(10,2);not null"`
	DeliveryFee      float64     `json:"deliveryFee" gorm:"type:decimal(10,2);default:0"`
	GrandTotal       float64     `json:"grandTotal" gorm:"type:decimal(10,2);not null"`
	DeliveryAddress  string      `json:"deliveryAddress" gorm:"type:text;not null"`
	PaymentMethod    string      `json:"paymentMethod" gorm:"type:varchar(20);not null"` // CARD, TRANSFER
	PaymentStatus    string      `json:"paymentStatus" gorm:"type:varchar(20);default:'PENDING'"` // PENDING, PAID, FAILED
	PaymentReference string      `json:"paymentReference" gorm:"type:varchar(255)"`
	Status           string      `json:"status" gorm:"type:varchar(20);default:'PENDING'"` // PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
	CreatedAt        time.Time   `json:"createdAt" gorm:"autoCreateTime"`
	UpdatedAt        time.Time   `json:"updatedAt" gorm:"autoUpdateTime"`
	
	// Relationships
	Items            []OrderItem `json:"items" gorm:"foreignKey:OrderID"`
}

// BeforeCreate hook to generate UUID and order number
func (o *Order) BeforeCreate(tx *gorm.DB) error {
	if o.ID == uuid.Nil {
		o.ID = uuid.New()
	}
	if o.OrderNumber == "" {
		o.OrderNumber = generateOrderNumber()
	}
	return nil
}

// TableName specifies the table name
func (Order) TableName() string {
	return "orders"
}

// OrderItem represents an item in an order
type OrderItem struct {
	ID          uuid.UUID `json:"id" gorm:"type:uuid;primary_key"`
	OrderID     uuid.UUID `json:"orderId" gorm:"type:uuid;not null"`
	ProductID   uuid.UUID `json:"productId" gorm:"type:uuid"`
	ProductName string    `json:"productName" gorm:"type:varchar(255);not null"`
	Grade       string    `json:"grade" gorm:"type:char(1);not null"`
	QuantityKg  float64   `json:"quantityKg" gorm:"type:decimal(10,2);not null"`
	PricePerKg  float64   `json:"pricePerKg" gorm:"type:decimal(10,2);not null"`
	Subtotal    float64   `json:"subtotal" gorm:"type:decimal(10,2);not null"`
}

// BeforeCreate hook to generate UUID
func (oi *OrderItem) BeforeCreate(tx *gorm.DB) error {
	if oi.ID == uuid.Nil {
		oi.ID = uuid.New()
	}
	return nil
}

// TableName specifies the table name
func (OrderItem) TableName() string {
	return "order_items"
}

// generateOrderNumber creates a unique order number
func generateOrderNumber() string {
	now := time.Now()
	return "ORD-" + now.Format("20060102") + "-" + uuid.New().String()[:8]
}