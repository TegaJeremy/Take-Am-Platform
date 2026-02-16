package models

import (
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type Product struct {
	ID              uuid.UUID      `gorm:"type:uuid;default:gen_random_uuid();primaryKey"`
	ProductName     string         `gorm:"type:varchar(255);not null"`
	Grade           string         `gorm:"type:varchar(10);not null"`
	AvailableWeight float64        `gorm:"type:decimal(10,2);not null"`
	PricePerKg      float64        `gorm:"type:decimal(10,2);not null"`
	Description     string         `gorm:"type:text"`
	Location        string         `gorm:"type:varchar(255)"`
	Status          string         `gorm:"type:varchar(20);default:'AVAILABLE'"`
	GradingID       *uuid.UUID     `gorm:"type:uuid"`
	TraderID        *uuid.UUID     `gorm:"type:uuid"`
	CreatedByAdmin  uuid.UUID      `gorm:"type:uuid;not null"`
	ExpiresAt       *time.Time     `gorm:"type:timestamptz"`
	CreatedAt       time.Time      `gorm:"type:timestamptz;autoCreateTime"`
	UpdatedAt       time.Time      `gorm:"type:timestamptz;autoUpdateTime"`
	Images          []ProductImage `gorm:"foreignKey:ProductID"`
}


func (p *Product) BeforeCreate(tx *gorm.DB) error {
	if p.ID == uuid.Nil {
		p.ID = uuid.New()
	}
	return nil
}


func (Product) TableName() string {
	return "products"
}