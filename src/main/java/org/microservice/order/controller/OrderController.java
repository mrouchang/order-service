package org.microservice.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.mzc.markets.common.model.*;
import org.microservice.order.service.OrderPublisherService;
import org.microservice.order.service.OrderStatusMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderPublisherService orderPublisherService;

    @PostMapping("/submit/stock")
    public ResponseEntity<Map<String, Object>> submitStockOrder(
            @RequestParam String symbol,
            @RequestParam BigDecimal quantity,
            @RequestParam BigDecimal price,
            @RequestParam OrderType orderType) {
        
        String orderId = UUID.randomUUID().toString();
        log.info("REST: Submitting stock order: orderId={}, symbol={}", orderId, symbol);
        
        StockDetails stockDetails = StockDetails.builder()
                .exchange("NASDAQ")
                .sector("Technology")
                .build();
        
        FinancialOrder order = FinancialOrder.builder()
                .orderId(orderId)
                .clientId("client-123")
                .orderType(orderType)
                .instrumentType(InstrumentType.STOCK)
                .symbol(symbol)
                .quantity(quantity)
                .price(price)
                .totalValue(quantity.multiply(price))
                .status(OrderStatus.PENDING)
                .tif(OrderTimeInForce.DAY)
                .currencyCode("USD")
                .createdAt(LocalDateTime.now())
                .details(stockDetails)
                .build();
        
        orderPublisherService.publishOrderToQueue(orderId, order);
        orderPublisherService.publishOrderToTopic(orderId, order);
        orderPublisherService.broadcastOrderStatus(orderId, OrderStatus.PENDING, order);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("orderId", orderId);
        response.put("order", order);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit/option")
    public ResponseEntity<Map<String, Object>> submitOptionOrder(
            @RequestParam String symbol,
            @RequestParam BigDecimal quantity,
            @RequestParam BigDecimal strikePrice,
            @RequestParam BigDecimal price,
            @RequestParam OptionType optionType) {
        
        String orderId = UUID.randomUUID().toString();
        log.info("REST: Submitting option order: orderId={}, symbol={}", orderId, symbol);
        
        OptionDetails optionDetails = OptionDetails.builder()
                .optionType(optionType)
                .strikePrice(strikePrice)
                .underlying(symbol)
                .impliedVolatility(BigDecimal.valueOf(0.25))
                .delta(BigDecimal.valueOf(0.65))
                .contractMultiplier(100)
                .build();
        
        FinancialOrder order = FinancialOrder.builder()
                .orderId(orderId)
                .clientId("client-123")
                .orderType(OrderType.BUY)
                .instrumentType(InstrumentType.OPTION)
                .symbol(symbol)
                .quantity(quantity)
                .price(price)
                .totalValue(quantity.multiply(price).multiply(BigDecimal.valueOf(100)))
                .status(OrderStatus.PENDING)
                .tif(OrderTimeInForce.DAY)
                .currencyCode("USD")
                .createdAt(LocalDateTime.now())
                .details(optionDetails)
                .build();
        
        orderPublisherService.publishOrderToQueue(orderId, order);
        orderPublisherService.publishOrderToTopic(orderId, order);
        orderPublisherService.broadcastOrderStatus(orderId, OrderStatus.PENDING, order);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("orderId", orderId);
        response.put("order", order);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit/bond")
    public ResponseEntity<Map<String, Object>> submitBondOrder(
            @RequestParam String symbol,
            @RequestParam BigDecimal quantity,
            @RequestParam BigDecimal price,
            @RequestParam OrderType orderType) {
        
        String orderId = UUID.randomUUID().toString();
        log.info("REST: Submitting bond order: orderId={}, symbol={}", orderId, symbol);
        
        BondDetails bondDetails = BondDetails.builder()
                .issuer("US Treasury")
                .rating("AAA")
                .couponRate(BigDecimal.valueOf(0.03))
                .yieldToMaturity(BigDecimal.valueOf(0.035))
                .bondType("GOVERNMENT")
                .build();
        
        FinancialOrder order = FinancialOrder.builder()
                .orderId(orderId)
                .clientId("client-123")
                .orderType(orderType)
                .instrumentType(InstrumentType.BOND)
                .symbol(symbol)
                .quantity(quantity)
                .price(price)
                .totalValue(quantity.multiply(price))
                .status(OrderStatus.PENDING)
                .tif(OrderTimeInForce.GTC)
                .currencyCode("USD")
                .createdAt(LocalDateTime.now())
                .details(bondDetails)
                .build();
        
        orderPublisherService.publishOrderToQueue(orderId, order);
        orderPublisherService.publishOrderToTopic(orderId, order);
        orderPublisherService.broadcastOrderStatus(orderId, OrderStatus.PENDING, order);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("orderId", orderId);
        response.put("order", order);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Order Service is running with Apache Artemis AMQP & WebSocket");
    }

    // WebSocket endpoint - receive orders from browsers
    @MessageMapping("/orders/submit")
    @SendTo("/topic/orders/status")
    public OrderStatusMessage receiveOrderFromBrowser(FinancialOrder order) {
        log.info("WebSocket: Received order from browser: orderId={}, instrumentType={}", 
                 order.getOrderId(), order.getInstrumentType());
        
        String orderId = order.getOrderId() != null ? order.getOrderId() : UUID.randomUUID().toString();
        order.setOrderId(orderId);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        
        orderPublisherService.publishOrderToQueue(orderId, order);
        orderPublisherService.publishOrderToTopic(orderId, order);
        
        OrderStatusMessage statusMessage = new OrderStatusMessage();
        statusMessage.setOrderId(orderId);
        statusMessage.setStatus(OrderStatus.PENDING);
        statusMessage.setDetails(order);
        statusMessage.setTimestamp(System.currentTimeMillis());
        
        return statusMessage;
    }

    // WebSocket endpoint - get order updates
    @MessageMapping("/orders/updates")
    public void orderUpdates(String orderId) {
        log.info("WebSocket: Client subscribed to order updates: {}", orderId);
    }
}
