package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)


type Cart struct {
	ID        uuid.UUID  `json:"id" gorm:"type:uuid;primary_key"`
	BuyerID   uuid.UUID  `json:"buyerId" gorm:"type:uuid;not null;uniqueIndex"`
	CreatedAt time.Time  `json:"createdAt" gorm:"autoCreateTime"`
	UpdatedAt time.Time  `json:"updatedAt" gorm:"autoUpdateTime"`
	
	// Relationships
	Items     []CartItem `json:"items" gorm:"foreignKey:CartID"`
}


func (c *Cart) BeforeCreate(tx *gorm.DB) error {
	if c.ID == uuid.Nil {
		c.ID = uuid.New()
	}
	return nil
}


func (Cart) TableName() string {
	return "carts"
}


type CartItem struct {
	ID         uuid.UUID `json:"id" gorm:"type:uuid;primary_key"`
	CartID     uuid.UUID `json:"cartId" gorm:"type:uuid;not null"`
	ProductID  uuid.UUID `json:"productId" gorm:"type:uuid;not null"`
	QuantityKg float64   `json:"quantityKg" gorm:"type:decimal(10,2);not null"`
	PricePerKg float64   `json:"pricePerKg" gorm:"type:decimal(10,2);not null"`
	Subtotal   float64   `json:"subtotal" gorm:"type:decimal(10,2);not null"`
	AddedAt    time.Time `json:"addedAt" gorm:"autoCreateTime"`
	
	// Relationships
	Product    Product   `json:"product" gorm:"foreignKey:ProductID"`
}


func (ci *CartItem) BeforeCreate(tx *gorm.DB) error {
	if ci.ID == uuid.Nil {
		ci.ID = uuid.New()
	}
	return nil
}


func (CartItem) TableName() string {
	return "cart_items"
}