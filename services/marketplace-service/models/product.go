package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// Product represents an item in the marketplace
type Product struct {
	ID              uuid.UUID       `json:"id" gorm:"type:uuid;primary_key"`
	ProductName     string          `json:"productName" gorm:"type:varchar(255);not null"`
	Grade           string          `json:"grade" gorm:"type:char(1);not null"` // A, B, C, D
	AvailableWeight float64         `json:"availableWeight" gorm:"type:decimal(10,2);not null"`
	PricePerKg      float64         `json:"pricePerKg" gorm:"type:decimal(10,2);not null"`
	Description     string          `json:"description" gorm:"type:text"`
	Location        string          `json:"location" gorm:"type:varchar(255)"`
	Status          string          `json:"status" gorm:"type:varchar(20);default:'AVAILABLE'"` // AVAILABLE, LOW_STOCK, SOLD_OUT
	GradingID       *uuid.UUID      `json:"gradingId" gorm:"type:uuid"` // Link to intake service grading
	TraderID        *uuid.UUID      `json:"traderId" gorm:"type:uuid"`
	CreatedByAdmin  uuid.UUID       `json:"createdByAdmin" gorm:"type:uuid;not null"`
	ExpiresAt       *time.Time      `json:"expiresAt"`
	CreatedAt       time.Time       `json:"createdAt" gorm:"autoCreateTime"`
	UpdatedAt       time.Time       `json:"updatedAt" gorm:"autoUpdateTime"`
	
	// Relationships
	Images          []ProductImage  `json:"images" gorm:"foreignKey:ProductID"`
}

// BeforeCreate hook to generate UUID
func (p *Product) BeforeCreate(tx *gorm.DB) error {
	if p.ID == uuid.Nil {
		p.ID = uuid.New()
	}
	return nil
}

// TableName specifies the table name
func (Product) TableName() string {
	return "products"
}