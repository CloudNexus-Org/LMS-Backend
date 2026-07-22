package com.lms.payment.controller;

import com.lms.payment.dto.*;
import com.lms.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ─── HEALTH ──────────────────────────────────────────────────────────────

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("service", "payment-service", "status", "UP");
    }

    // ─── CART (4 endpoints) ──────────────────────────────────────────────────

    /** 1. GET /api/payments/cart */
    @GetMapping("/cart")
    public List<CartItemResponse> getCart(@RequestHeader("X-User-Id") Long userId) {
        return paymentService.getCart(userId);
    }

    /** 2. POST /api/payments/cart/items */
    @PostMapping("/cart/items")
    public ResponseEntity<CartItemResponse> addToCart(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CartItemRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.addToCart(userId, req));
    }

    /** 3. PUT /api/payments/cart/items/{itemId} */
    @PutMapping("/cart/items/{itemId}")
    public CartItemResponse updateCartItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody CartItemRequest req) {
        return paymentService.updateCartItem(userId, itemId, req);
    }

    /** 4. DELETE /api/payments/cart/items/{itemId} */
    @DeleteMapping("/cart/items/{itemId}")
    public ResponseEntity<Void> removeFromCart(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId) {
        paymentService.removeFromCart(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    /** 5. DELETE /api/payments/cart */
    @DeleteMapping("/cart")
    public ResponseEntity<Void> clearCart(@RequestHeader("X-User-Id") Long userId) {
        paymentService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    // ─── WISHLIST (3 endpoints) ───────────────────────────────────────────────

    /** 6. GET /api/payments/wishlist */
    @GetMapping("/wishlist")
    public List<WishlistItemResponse> getWishlist(@RequestHeader("X-User-Id") Long userId) {
        return paymentService.getWishlist(userId);
    }

    /** 7. POST /api/payments/wishlist/items */
    @PostMapping("/wishlist/items")
    public ResponseEntity<WishlistItemResponse> addToWishlist(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody WishlistItemRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.addToWishlist(userId, req));
    }

    /** 8. DELETE /api/payments/wishlist/items/{itemId} */
    @DeleteMapping("/wishlist/items/{itemId}")
    public ResponseEntity<Void> removeFromWishlist(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId) {
        paymentService.removeFromWishlist(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    // ─── CHECKOUT + PAYMENT (3 endpoints) ────────────────────────────────────

    /** 9. POST /api/payments/checkout */
    @PostMapping("/checkout")
    public CheckoutResponse checkout(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody(required = false) CheckoutRequest req) {
        return paymentService.checkout(userId, req != null ? req : new CheckoutRequest());
    }

    /** 10. POST /api/payments/initiate */
    @PostMapping("/initiate")
    public InitiatePaymentResponse initiatePayment(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody(required = false) InitiatePaymentRequest req) {
        return paymentService.initiatePayment(userId, req != null ? req : new InitiatePaymentRequest());
    }

    /** 11. POST /api/payments/webhook — called by Razorpay after payment */
    @PostMapping("/webhook")
    public Map<String, String> webhook(@RequestBody WebhookRequest req) {
        return paymentService.handleWebhook(req);
    }

    // ─── ORDERS (3 endpoints) ─────────────────────────────────────────────────

    /** 12. GET /api/payments/orders/me */
    @GetMapping("/orders/me")
    public List<OrderResponse> getMyOrders(@RequestHeader("X-User-Id") Long userId) {
        return paymentService.getMyOrders(userId);
    }

    /** 13. GET /api/payments/orders/{orderId} */
    @GetMapping("/orders/{orderId}")
    public OrderResponse getOrder(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long orderId) {
        return paymentService.getOrder(userId, orderId);
    }

    /** 14. GET /api/payments/orders/{orderId}/invoice */
    @GetMapping("/orders/{orderId}/invoice")
    public Map<String, String> getInvoice(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long orderId) {
        return paymentService.getInvoice(userId, orderId);
    }
}
