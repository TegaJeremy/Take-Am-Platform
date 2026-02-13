package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

// ProductImage represents an image for a product
type ProductImage struct {
	ID           uuid.UUID `json:"id" gorm:"type:uuid;primary_key"`
	ProductID    uuid.UUID `json:"productId" gorm:"type:uuid;not null"`
	ImageURL     string    `json:"imageUrl" gorm:"type:text;not null"`
	IsPrimary    bool      `json:"isPrimary" gorm:"default:false"`
	DisplayOrder int       `json:"displayOrder" gorm:"default:0"`
	UploadedBy   uuid.UUID `json:"uploadedBy" gorm:"type:uuid;not null"` // Admin user ID
	UploadedAt   time.Time `json:"uploadedAt" gorm:"autoCreateTime"`
}

// BeforeCreate hook to generate UUID
func (pi *ProductImage) BeforeCreate(tx *gorm.DB) error {
	if pi.ID == uuid.Nil {
		pi.ID = uuid.New()
	}
	return nil
}

// TableName specifies the table name
func (ProductImage) TableName() string {
	return "product_images"
}