package utils

import (
	"errors"
	"regexp"
	"strings"
)

// ValidateProductName validates product name
func ValidateProductName(name string) error {
	name = strings.TrimSpace(name)
	if name == "" {
		return errors.New("product name is required")
	}
	if len(name) < 3 {
		return errors.New("product name must be at least 3 characters")
	}
	if len(name) > 255 {
		return errors.New("product name must not exceed 255 characters")
	}
	return nil
}

// ValidateGrade validates product grade (Grade A, Grade B, Grade C, Grade D)
func ValidateGrade(grade string) error {
	grade = strings.TrimSpace(grade)
	if grade == "" {
		return errors.New("grade is required")
	}

	// Accept both formats: "A" or "Grade A"
	validGrades := []string{"A", "B", "C", "D", "Grade A", "Grade B", "Grade C", "Grade D"}
	
	gradeUpper := strings.ToUpper(grade)
	for _, valid := range validGrades {
		if strings.ToUpper(valid) == gradeUpper {
			return nil
		}
	}

	return errors.New("grade must be one of: Grade A, Grade B, Grade C, Grade D")
}

// ValidateWeight validates available weight
func ValidateWeight(weight float64) error {
	if weight <= 0 {
		return errors.New("available weight must be greater than 0")
	}
	if weight > 100000 {
		return errors.New("available weight must not exceed 100,000 kg")
	}
	return nil
}

// ValidatePrice validates price per kg
func ValidatePrice(price float64) error {
	if price <= 0 {
		return errors.New("price per kg must be greater than 0")
	}
	if price > 1000000 {
		return errors.New("price per kg must not exceed 1,000,000")
	}
	return nil
}

// ValidateLocation validates location
func ValidateLocation(location string) error {
	location = strings.TrimSpace(location)
	if location == "" {
		return errors.New("location is required")
	}
	if len(location) < 2 {
		return errors.New("location must be at least 2 characters")
	}
	if len(location) > 255 {
		return errors.New("location must not exceed 255 characters")
	}
	return nil
}

// ValidateDescription validates description (optional but has max length)
func ValidateDescription(description string) error {
	description = strings.TrimSpace(description)
	if len(description) > 5000 {
		return errors.New("description must not exceed 5000 characters")
	}
	return nil
}

// ValidateUUID validates UUID format
func ValidateUUID(id string) error {
	if id == "" {
		return nil // Optional field
	}
	
	uuidRegex := regexp.MustCompile(`^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$`)
	if !uuidRegex.MatchString(strings.ToLower(id)) {
		return errors.New("invalid UUID format")
	}
	return nil
}

// ValidateStatus validates product status
func ValidateStatus(status string) error {
	if status == "" {
		return nil // Will use default
	}

	validStatuses := []string{"AVAILABLE", "OUT_OF_STOCK", "DISCONTINUED", "PENDING"}
	statusUpper := strings.ToUpper(status)
	
	for _, valid := range validStatuses {
		if valid == statusUpper {
			return nil
		}
	}

	return errors.New("status must be one of: AVAILABLE, OUT_OF_STOCK, DISCONTINUED, PENDING")
}

// ValidateImageCount validates number of images
func ValidateImageCount(count int) error {
	if count > 3 {
		return errors.New("maximum 3 images allowed per product")
	}
	return nil
}

// ValidateImageFile validates image file type and size
func ValidateImageFile(filename string, size int64) error {
	// Check file size (max 5MB)
	maxSize := int64(5 * 1024 * 1024) // 5MB
	if size > maxSize {
		return errors.New("image file size must not exceed 5MB")
	}

	// Check file extension
	validExtensions := []string{".jpg", ".jpeg", ".png", ".webp"}
	filenameLower := strings.ToLower(filename)
	
	valid := false
	for _, ext := range validExtensions {
		if strings.HasSuffix(filenameLower, ext) {
			valid = true
			break
		}
	}

	if !valid {
		return errors.New("image must be in JPG, JPEG, PNG, or WEBP format")
	}

	return nil
}