package services

import (
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"

	"github.com/google/uuid"
	"marketplace-service/database"
	"marketplace-service/models"
)

type PaymentService struct {
	secretKey string
}

func NewPaymentService(secretKey string) *PaymentService {
	return &PaymentService{
		secretKey: secretKey,
	}
}

type PaystackInitializeRequest struct {
	Email       string `json:"email"`
	Amount      int    `json:"amount"`
	Reference   string `json:"reference"`
	CallbackURL string `json:"callback_url,omitempty"`
}

type PaystackInitializeResponse struct {
	Status  bool   `json:"status"`
	Message string `json:"message"`
	Data    struct {
		AuthorizationURL string `json:"authorization_url"`
		AccessCode       string `json:"access_code"`
		Reference        string `json:"reference"`
	} `json:"data"`
}

type PaystackVerifyResponse struct {
	Status  bool   `json:"status"`
	Message string `json:"message"`
	Data    struct {
		Status   string `json:"status"`
		Reference string `json:"reference"`
		Amount   int    `json:"amount"`
		Currency string `json:"currency"`
		Customer struct {
			Email string `json:"email"`
		} `json:"customer"`
	} `json:"data"`
}

func (s *PaymentService) InitializePayment(orderID, buyerEmail, callbackURL string) (*PaystackInitializeResponse, error) {
	db := database.GetDB()

	var order models.Order
	if err := db.First(&order, "id = ?", orderID).Error; err != nil {
		return nil, errors.New("order not found")
	}

	if order.PaymentStatus == "PAID" {
		return nil, errors.New("order already paid")
	}

	reference := "PAY-" + uuid.New().String()
	amountInKobo := int(order.GrandTotal * 100)

	if callbackURL == "" {
		callbackURL = "takeam://payment/callback"
	}

	requestBody := PaystackInitializeRequest{
		Email:       buyerEmail,
		Amount:      amountInKobo,
		Reference:   reference,
		CallbackURL: callbackURL,
	}

	jsonData, _ := json.Marshal(requestBody)

	req, err := http.NewRequest("POST", "https://api.paystack.co/transaction/initialize", bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, err
	}

	req.Header.Set("Authorization", "Bearer "+s.secretKey)
	req.Header.Set("Content-Type", "application/json")

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	body, _ := io.ReadAll(resp.Body)

	var paystackResp PaystackInitializeResponse
	if err := json.Unmarshal(body, &paystackResp); err != nil {
		return nil, err
	}

	if !paystackResp.Status {
		return nil, errors.New(paystackResp.Message)
	}

	order.PaymentReference = reference
	db.Save(&order)

	return &paystackResp, nil
}

func (s *PaymentService) VerifyPayment(reference string) error {
	db := database.GetDB()

	url := fmt.Sprintf("https://api.paystack.co/transaction/verify/%s", reference)
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return err
	}

	req.Header.Set("Authorization", "Bearer "+s.secretKey)

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	body, _ := io.ReadAll(resp.Body)

	var verifyResp PaystackVerifyResponse
	if err := json.Unmarshal(body, &verifyResp); err != nil {
		return err
	}

	if !verifyResp.Status {
		return errors.New(verifyResp.Message)
	}

	if verifyResp.Data.Status != "success" {
		return errors.New("payment not successful")
	}

	var order models.Order
	if err := db.Where("payment_reference = ?", reference).First(&order).Error; err != nil {
		return errors.New("order not found")
	}

	expectedAmountInKobo := int(order.GrandTotal * 100)
	paidAmountInKobo := verifyResp.Data.Amount

	if paidAmountInKobo != expectedAmountInKobo {
		return fmt.Errorf("payment amount mismatch: expected ₦%.2f but received ₦%.2f",
			order.GrandTotal,
			float64(paidAmountInKobo)/100)
	}

	if verifyResp.Data.Currency != "NGN" {
		return errors.New("invalid currency: expected NGN")
	}

	order.PaymentStatus = "PAID"
	order.Status = "CONFIRMED"
	db.Save(&order)

	return nil
}