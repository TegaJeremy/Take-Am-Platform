package payment

import (
	"crypto/hmac"
	"crypto/sha512"
	"encoding/hex"
	"encoding/json"
	"io"
	"log"
	"net/http"

	"github.com/gin-gonic/gin"
)

type PaystackWebhookEvent struct {
	Event string `json:"event"`
	Data  struct {
		Reference string `json:"reference"`
		Status    string `json:"status"`
		Amount    int    `json:"amount"`
		Channel   string `json:"channel"`
	} `json:"data"`
}

func (h *PaymentHandler) PaystackWebhook(c *gin.Context) {
	body, err := io.ReadAll(c.Request.Body)
	if err != nil {
		log.Println("Webhook: Failed to read body:", err)
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request"})
		return
	}

	signature := c.GetHeader("x-paystack-signature")
	if !h.verifyPaystackSignature(body, signature) {
		log.Println("Webhook: Invalid signature")
		c.JSON(http.StatusUnauthorized, gin.H{"error": "Invalid signature"})
		return
	}

	var event PaystackWebhookEvent
	if err := json.Unmarshal(body, &event); err != nil {
		log.Println("Webhook: Failed to parse JSON:", err)
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid payload"})
		return
	}

	log.Printf("Webhook: Received event %s for reference %s", event.Event, event.Data.Reference)

	if event.Event == "charge.success" && event.Data.Status == "success" {
		err := h.paymentService.VerifyPayment(event.Data.Reference)
		if err != nil {
			log.Println("Webhook: Verification failed:", err)
			c.JSON(http.StatusOK, gin.H{"status": "acknowledged"})
			return
		}

		log.Printf("Webhook: Payment verified for reference %s", event.Data.Reference)
	}

	c.JSON(http.StatusOK, gin.H{"status": "success"})
}

func (h *PaymentHandler) verifyPaystackSignature(body []byte, signature string) bool {
	secret := h.secretKey

	mac := hmac.New(sha512.New, []byte(secret))
	mac.Write(body)
	expectedSignature := hex.EncodeToString(mac.Sum(nil))

	return hmac.Equal([]byte(signature), []byte(expectedSignature))
}