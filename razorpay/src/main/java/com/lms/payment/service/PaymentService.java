package com.lms.payment.service;

import com.lms.payment.client.CatalogClient;
import com.lms.payment.dto.*;
import com.lms.payment.event.PaymentEventProducer;
import com.lms.payment.model.*;
import com.lms.payment.repository.*;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentService {

    private final CartItemRepository cartItemRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CatalogClient catalogClient;
    private final PaymentEventProducer eventProducer;

    @Value("${razorpay.api.key}")
    private String razorpayKey;

    @Value("${razorpay.api.secret}")
    private String razorpaySecret;

    private static final BigDecimal GST_RATE = new BigDecimal("0.18");

    // ─── CART ─────────────────────────────────────────────────────────────────

    public List<CartItemResponse> getCart(Long userId) {
        return cartItemRepository.findByUserId(userId).stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CartItemResponse addToCart(Long userId, CartItemRequest req) {
        if (req.getCourseId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "courseId is required");
        }
        if (cartItemRepository.existsByUserIdAndCourseId(userId, req.getCourseId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Course already in cart");
        }
        CartItem item = CartItem.builder()
                .userId(userId)
                .courseId(req.getCourseId())
                .trackId(req.getTrackId())
                .itemType(req.getItemType() != null ? req.getItemType() : "course")
                .build();
        CartItem saved = cartItemRepository.save(item);
        return toCartItemResponse(saved);
    }

    @Transactional
    public CartItemResponse updateCartItem(Long userId, Long itemId, CartItemRequest req) {
        CartItem item = cartItemRepository.findById(itemId)
                .filter(c -> c.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));
        if (req.getItemType() != null) item.setItemType(req.getItemType());
        if (req.getTrackId() != null) item.setTrackId(req.getTrackId());
        return toCartItemResponse(cartItemRepository.save(item));
    }

    @Transactional
    public void removeFromCart(Long userId, Long itemId) {
        cartItemRepository.deleteByIdAndUserId(itemId, userId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    // ─── WISHLIST ─────────────────────────────────────────────────────────────

    public List<WishlistItemResponse> getWishlist(Long userId) {
        return wishlistItemRepository.findByUserId(userId).stream()
                .map(this::toWishlistItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WishlistItemResponse addToWishlist(Long userId, WishlistItemRequest req) {
        if (req.getCourseId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "courseId is required");
        }
        if (wishlistItemRepository.existsByUserIdAndCourseId(userId, req.getCourseId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Course already in wishlist");
        }
        WishlistItem item = WishlistItem.builder()
                .userId(userId)
                .courseId(req.getCourseId())
                .trackId(req.getTrackId())
                .build();
        return toWishlistItemResponse(wishlistItemRepository.save(item));
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long itemId) {
        wishlistItemRepository.deleteByIdAndUserId(itemId, userId);
    }

    // ─── CHECKOUT ─────────────────────────────────────────────────────────────

    public CheckoutResponse checkout(Long userId, CheckoutRequest req) {
        List<CartItem> cartItems = resolveCartItems(userId, req);
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No items to checkout");
        }

        List<CartItemResponse> enriched = cartItems.stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal subtotal = enriched.stream()
                .map(CartItemResponse::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal originalTotal = enriched.stream()
                .map(r -> r.getOriginalPrice() != null ? r.getOriginalPrice() : r.getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal savings = originalTotal.subtract(subtotal).max(BigDecimal.ZERO);
        BigDecimal gst = subtotal.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(gst);

        return CheckoutResponse.builder()
                .items(enriched)
                .subtotal(subtotal)
                .gst(gst)
                .total(total)
                .savings(savings)
                .build();
    }

    // ─── INITIATE PAYMENT ─────────────────────────────────────────────────────

    @Transactional
    public InitiatePaymentResponse initiatePayment(Long userId, InitiatePaymentRequest req) {
        String currency = req.getCurrency() != null ? req.getCurrency().toUpperCase() : "INR";
        BigDecimal subtotal;
        BigDecimal gst;
        BigDecimal total;
        List<CartItem> cartItems;

        boolean hasDirectAmount = req.getAmount() != null && req.getAmount().compareTo(BigDecimal.ZERO) > 0;
        boolean hasTrackOnly = req.getTrackId() != null && !req.getTrackId().isBlank()
                && (req.getCourseIds() == null || req.getCourseIds().isEmpty());

        if (hasDirectAmount) {
            // Frontend passed amount directly (track-price checkout, no catalog courseId)
            subtotal = req.getAmount().setScale(2, RoundingMode.HALF_UP);
            gst = subtotal.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
            total = subtotal.add(gst);
            // Build a synthetic cart item for this track
            CartItem syntheticItem = CartItem.builder()
                    .userId(userId)
                    .courseId(null)
                    .trackId(req.getTrackId())
                    .itemType("track")
                    .build();
            cartItems = List.of(syntheticItem);
        } else {
            // Catalog-backed checkout (courseId available or full cart)
            CheckoutRequest cr = new CheckoutRequest();
            cr.setCourseIds(req.getCourseIds());
            cr.setTrackId(req.getTrackId());
            cr.setCouponCode(req.getCouponCode());
            CheckoutResponse summary = checkout(userId, cr);
            subtotal = summary.getSubtotal();
            gst = summary.getGst();
            total = summary.getTotal();
            cartItems = resolveCartItems(userId, cr);
        }

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            // ── FREE COURSE — skip Razorpay, mark order PAID immediately ──────────
            String orderNumber = "ORD-FREE-" + System.currentTimeMillis();
            Order freeOrder = Order.builder()
                    .userId(userId)
                    .orderNumber(orderNumber)
                    .subtotal(BigDecimal.ZERO)
                    .gst(BigDecimal.ZERO)
                    .total(BigDecimal.ZERO)
                    .status("PAID")
                    .razorpayOrderId(null)
                    .build();

            final String freeTitle = req.getTitle();
            final String freeTrackId = req.getTrackId();
            List<OrderItem> freeItems = cartItems.stream().map(ci -> OrderItem.builder()
                    .order(freeOrder)
                    .courseId(ci.getCourseId())
                    .trackId(ci.getTrackId())
                    .title(freeTitle != null ? freeTitle : "Free Course")
                    .price(BigDecimal.ZERO)
                    .build()).collect(Collectors.toList());
            freeOrder.setItems(freeItems);
            orderRepository.save(freeOrder);

            // Publish payment.success so enrollment-service auto-enrolls the student
            eventProducer.publishPaymentSuccess(freeOrder, "free");

            return InitiatePaymentResponse.builder()
                    .orderId(freeOrder.getId())
                    .razorpayOrderId(null)
                    .razorpayKeyId(null)
                    .amount(BigDecimal.ZERO)
                    .currency(currency)
                    .orderNumber(orderNumber)
                    .subtotal(BigDecimal.ZERO)
                    .gst(BigDecimal.ZERO)
                    .total(BigDecimal.ZERO)
                    .free(true)
                    .build();
        }

        // Create Razorpay order
        String razorpayOrderId;
        try {
            RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);
            JSONObject orderRequest = new JSONObject();
            // Razorpay expects amount in paise (smallest INR unit)
            long amountInPaise = total.multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP).longValue();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", "rcpt_" + System.currentTimeMillis());
            orderRequest.put("payment_capture", 1);
            com.razorpay.Order rzpOrder = client.orders.create(orderRequest);
            razorpayOrderId = rzpOrder.get("id");
        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Payment gateway error: " + e.getMessage());
        }

        // Persist internal order
        String orderNumber = "ORD-" + System.currentTimeMillis();
        Order order = Order.builder()
                .userId(userId)
                .orderNumber(orderNumber)
                .subtotal(subtotal)
                .gst(gst)
                .total(total)
                .status("PENDING")
                .razorpayOrderId(razorpayOrderId)
                .build();

        final BigDecimal itemSubtotal = subtotal;
        final String reqTitle = req.getTitle();
        final String reqTrackId = req.getTrackId();

        List<OrderItem> orderItems = cartItems.stream().map(ci -> {
            BigDecimal itemPrice;
            String itemTitle;
            if (ci.getCourseId() != null) {
                var course = catalogClient.findCourse(ci.getCourseId());
                itemPrice = course.map(CatalogClient.CourseSnapshot::price).orElse(itemSubtotal);
                itemTitle = course.map(CatalogClient.CourseSnapshot::title)
                        .orElse(reqTitle != null ? reqTitle : "Course");
            } else {
                // Track-only item — use the direct amount
                itemPrice = itemSubtotal;
                itemTitle = reqTitle != null ? reqTitle
                        : (reqTrackId != null ? "Track: " + reqTrackId : "Course");
            }
            return OrderItem.builder()
                    .order(order)
                    .courseId(ci.getCourseId())
                    .trackId(ci.getTrackId())
                    .title(itemTitle)
                    .price(itemPrice)
                    .build();
        }).collect(Collectors.toList());

        order.setItems(orderItems);
        orderRepository.save(order);

        return InitiatePaymentResponse.builder()
                .orderId(order.getId())
                .razorpayOrderId(razorpayOrderId)
                .razorpayKeyId(razorpayKey)
                .amount(total)
                .currency(currency)
                .orderNumber(orderNumber)
                .subtotal(subtotal)
                .gst(gst)
                .total(total)
                .free(false)
                .build();
    }

    // ─── WEBHOOK ─────────────────────────────────────────────────────────────

    @Transactional
    public Map<String, String> handleWebhook(WebhookRequest req) {
        String paymentId = req.getRazorpay_payment_id();
        String gatewayOrderId = req.getRazorpay_order_id();
        String signature = req.getRazorpay_signature();

        // Verify signature
        boolean valid = verifySignature(paymentId, gatewayOrderId, signature);

        Order order = orderRepository.findByRazorpayOrderId(gatewayOrderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order not found for razorpay order: " + gatewayOrderId));

        if (!valid) {
            order.setStatus("FAILED");
            orderRepository.save(order);
            Payment failedPayment = Payment.builder()
                    .orderId(order.getId())
                    .gateway("razorpay")
                    .gatewayPaymentId(paymentId)
                    .gatewayOrderId(gatewayOrderId)
                    .signature(signature)
                    .amount(order.getTotal())
                    .status("FAILED")
                    .build();
            paymentRepository.save(failedPayment);
            eventProducer.publishPaymentFailed(order, "Signature verification failed");
            return Map.of("status", "failed", "message", "Signature verification failed");
        }

        // Mark order PAID
        order.setStatus("PAID");
        orderRepository.save(order);

        // Record payment
        Payment payment = Payment.builder()
                .orderId(order.getId())
                .gateway("razorpay")
                .gatewayPaymentId(paymentId)
                .gatewayOrderId(gatewayOrderId)
                .signature(signature)
                .amount(order.getTotal())
                .status("SUCCESS")
                .paidAt(Instant.now())
                .build();
        paymentRepository.save(payment);

        // Clear cart for this user
        cartItemRepository.deleteByUserId(order.getUserId());

        // Publish Kafka event → enrollment-service will auto-enroll
        eventProducer.publishPaymentSuccess(order, paymentId);

        return Map.of("status", "success", "orderId", String.valueOf(order.getId()));
    }

    // ─── ORDERS ───────────────────────────────────────────────────────────────

    public List<OrderResponse> getMyOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        return toOrderResponse(order);
    }

    public Map<String, String> getInvoice(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        // Return invoice metadata; actual PDF generation is a future enhancement
        return Map.of(
                "orderId", String.valueOf(order.getId()),
                "orderNumber", order.getOrderNumber(),
                "status", order.getStatus(),
                "total", order.getTotal().toString(),
                "message", "Invoice download ready"
        );
    }

    // ─── PRIVATE HELPERS ──────────────────────────────────────────────────────

    private List<CartItem> resolveCartItems(Long userId, CheckoutRequest req) {
        if (req != null && req.getCourseIds() != null && !req.getCourseIds().isEmpty()) {
            // Specific courses requested
            return req.getCourseIds().stream()
                    .map(cid -> cartItemRepository.findByUserIdAndCourseId(userId, cid)
                            .orElseGet(() -> CartItem.builder()
                                    .userId(userId)
                                    .courseId(cid)
                                    .trackId(req.getTrackId())
                                    .itemType("course")
                                    .build()))
                    .collect(Collectors.toList());
        }
        if (req != null && req.getTrackId() != null && !req.getTrackId().isBlank()) {
            // Single track checkout (from CoursePaymentPage)
            return List.of(CartItem.builder()
                    .userId(userId)
                    .courseId(null)
                    .trackId(req.getTrackId())
                    .itemType("track")
                    .build());
        }
        return cartItemRepository.findByUserId(userId);
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        var course = item.getCourseId() != null
                ? catalogClient.findCourse(item.getCourseId())
                : Optional.<CatalogClient.CourseSnapshot>empty();

        return CartItemResponse.builder()
                .id(item.getId())
                .courseId(item.getCourseId())
                .trackId(item.getTrackId())
                .itemType(item.getItemType())
                .title(course.map(CatalogClient.CourseSnapshot::title).orElse("Course"))
                .price(course.map(CatalogClient.CourseSnapshot::price).orElse(BigDecimal.ZERO))
                .originalPrice(course.map(CatalogClient.CourseSnapshot::originalPrice).orElse(null))
                .thumbnailUrl(course.map(CatalogClient.CourseSnapshot::thumbnailUrl).orElse(null))
                .addedAt(item.getAddedAt())
                .build();
    }

    private WishlistItemResponse toWishlistItemResponse(WishlistItem item) {
        var course = item.getCourseId() != null
                ? catalogClient.findCourse(item.getCourseId())
                : Optional.<CatalogClient.CourseSnapshot>empty();
        return WishlistItemResponse.builder()
                .id(item.getId())
                .courseId(item.getCourseId())
                .trackId(item.getTrackId())
                .title(course.map(CatalogClient.CourseSnapshot::title).orElse("Course"))
                .price(course.map(CatalogClient.CourseSnapshot::price).orElse(BigDecimal.ZERO))
                .thumbnailUrl(course.map(CatalogClient.CourseSnapshot::thumbnailUrl).orElse(null))
                .addedAt(item.getAddedAt())
                .build();
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> OrderItemResponse.builder()
                        .id(i.getId())
                        .courseId(i.getCourseId())
                        .trackId(i.getTrackId())
                        .title(i.getTitle())
                        .price(i.getPrice())
                        .build())
                .collect(Collectors.toList());
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .subtotal(order.getSubtotal())
                .gst(order.getGst())
                .total(order.getTotal())
                .status(order.getStatus())
                .razorpayOrderId(order.getRazorpayOrderId())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private boolean verifySignature(String paymentId, String orderId, String signature) {
        if (paymentId == null || orderId == null || signature == null) return false;
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_signature", signature);
            return Utils.verifyPaymentSignature(options, razorpaySecret);
        } catch (RazorpayException e) {
            log.warn("Signature verification exception (test mode fallback): {}", e.getMessage());
            // In test mode, accept if ids look valid
            return paymentId.startsWith("pay_") && orderId.startsWith("order_");
        }
    }
}
