package services

import (
	"context"
	"fmt"
	"mime/multipart"

	"github.com/cloudinary/cloudinary-go/v2"
	"github.com/cloudinary/cloudinary-go/v2/api/uploader"
)

type CloudinaryService struct {
	cld *cloudinary.Cloudinary
}

var cloudinaryService *CloudinaryService


func InitCloudinary(cloudName, apiKey, apiSecret string) error {
	cld, err := cloudinary.NewFromParams(cloudName, apiKey, apiSecret)
	if err != nil {
		return err
	}

	cloudinaryService = &CloudinaryService{cld: cld}
	return nil
}


func (s *CloudinaryService) UploadImage(file multipart.File, filename string) (string, error) {
	ctx := context.Background()

	uploadResult, err := s.cld.Upload.Upload(ctx, file, uploader.UploadParams{
		Folder:   "takeam/products",
		PublicID: filename,
	})

	if err != nil {
		return "", fmt.Errorf("failed to upload image: %w", err)
	}

	return uploadResult.SecureURL, nil
}


func (s *CloudinaryService) DeleteImage(publicID string) error {
	ctx := context.Background()

	_, err := s.cld.Upload.Destroy(ctx, uploader.DestroyParams{
		PublicID: publicID,
	})

	return err
}