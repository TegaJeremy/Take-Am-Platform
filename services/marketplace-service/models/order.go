package models

import (
	"fmt"
	"math/rand"
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

const (
	DeliveryStatusPending   = "PENDING"
	DeliveryStatusReady     = "READY"
	DeliveryStatusInTransit = "IN_TRANSIT"
	DeliveryStatusPickedUp  = "PICKED_UP"
	DeliveryStatusDelivered = "DELIVERED"
	DeliveryStatusCancelled = "CANCELLED"
	DeliveryStatusFailed    = "FAILED"
)

const (
	DeliveryTypePickup   = "PICKUP"
	DeliveryTypeDelivery = "DELIVERY"
)

type Order struct {
	ID               uuid.UUID   `json:"id" gorm:"type:uuid;primary_key"`
	OrderNumber      string      `json:"orderNumber" gorm:"type:varchar(50);unique;not null"`
	PickupCode       string      `json:"pickupCode" gorm:"type:varchar(20);unique"`
	BuyerID          uuid.UUID   `json:"buyerId" gorm:"type:uuid;not null"`
	Subtotal         float64     `json:"subtotal" gorm:"type:decimal(10,2);not null"`
	DeliveryFee      float64     `json:"deliveryFee" gorm:"type:decimal(10,2);default:0"`
	GrandTotal       float64     `json:"grandTotal" gorm:"type:decimal(10,2);not null"`
	DeliveryAddress  string      `json:"deliveryAddress" gorm:"type:text;not null"`
	DeliveryType     string      `json:"deliveryType" gorm:"type:varchar(20);default:'PICKUP'"`
	DeliveryStatus   string      `json:"deliveryStatus" gorm:"type:varchar(20);default:'PENDING'"`
	PaymentMethod    string      `json:"paymentMethod" gorm:"type:varchar(20);not null"`
	PaymentStatus    string      `json:"paymentStatus" gorm:"type:varchar(20);default:'PENDING'"`
	PaymentReference string      `json:"paymentReference" gorm:"type:varchar(255)"`
	Status           string      `json:"status" gorm:"type:varchar(20);default:'PENDING'"`
	PickedUpAt       *time.Time  `json:"pickedUpAt" gorm:"type:timestamp"`
	CreatedAt        time.Time   `json:"createdAt" gorm:"autoCreateTime"`
	UpdatedAt        time.Time   `json:"updatedAt" gorm:"autoUpdateTime"`
	Items            []OrderItem `json:"items" gorm:"foreignKey:OrderID"`
}

func (o *Order) BeforeCreate(tx *gorm.DB) error {
	if o.ID == uuid.Nil {
		o.ID = uuid.New()
	}
	if o.OrderNumber == "" {
		o.OrderNumber = generateOrderNumber()
	}
	if o.PickupCode == "" {
		o.PickupCode = generatePickupCode()
	}
	return nil
}

func (Order) TableName() string {
	return "orders"
}

type OrderItem struct {
	ID          uuid.UUID `json:"id" gorm:"type:uuid;primary_key"`
	OrderID     uuid.UUID `json:"orderId" gorm:"type:uuid;not null"`
	ProductID   uuid.UUID `json:"productId" gorm:"type:uuid"`
	ProductName string    `json:"productName" gorm:"type:varchar(255);not null"`
	Grade       string    `json:"grade" gorm:"type:varchar(10);not null"`
	QuantityKg  float64   `json:"quantityKg" gorm:"type:decimal(10,2);not null"`
	PricePerKg  float64   `json:"pricePerKg" gorm:"type:decimal(10,2);not null"`
	Subtotal    float64   `json:"subtotal" gorm:"type:decimal(10,2);not null"`
}

func (oi *OrderItem) BeforeCreate(tx *gorm.DB) error {
	if oi.ID == uuid.Nil {
		oi.ID = uuid.New()
	}
	return nil
}

func (OrderItem) TableName() string {
	return "order_items"
}

func generateOrderNumber() string {
	now := time.Now()
	return "ORD-" + now.Format("20060102") + "-" + uuid.New().String()[:8]
}

func generatePickupCode() string {
	rand.Seed(time.Now().UnixNano())
	code := rand.Intn(9000) + 1000
	return fmt.Sprintf("TKM-%d", code)
}