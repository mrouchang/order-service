package org.microservice.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.mzc.markets.common.model.FinancialOrder;
import com.mzc.markets.common.model.OrderStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPublisherService {

    private final JmsTemplate jmsTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    // Publish order to queue (for backend processing)
    public void publishOrderToQueue(String orderId, FinancialOrder order) {
        log.info("Publishing order to queue: orderId={}, instrumentType={}", orderId, order.getInstrumentType());
        jmsTemplate.convertAndSend("order-submit-queue", order);
    }

    // Publish order to topic (for subscribers/broadcasts)
    public void publishOrderToTopic(String orderId, FinancialOrder order) {
        log.info("Publishing order to topic: orderId={}, instrumentType={}", orderId, order.getInstrumentType());
        
        JmsTemplate topicTemplate = new JmsTemplate(jmsTemplate.getConnectionFactory());
        topicTemplate.setPubSubDomain(true); // Use Topic
        topicTemplate.convertAndSend("order-published-topic", order);
    }

    // Broadcast order status to connected browsers via WebSocket
    public void broadcastOrderStatus(String orderId, OrderStatus status, FinancialOrder order) {
        log.info("Broadcasting order status to browsers: orderId={}, status={}", orderId, status);
        
        OrderStatusMessage statusMessage = new OrderStatusMessage();
        statusMessage.setOrderId(orderId);
        statusMessage.setStatus(status);
        statusMessage.setDetails(order);
        statusMessage.setTimestamp(System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/orders/status", statusMessage);
    }

    // Send message to specific user
    public void sendToUser(String userId, String orderId, String message) {
        log.info("Sending message to user: userId={}, orderId={}", userId, orderId);
        messagingTemplate.convertAndSendToUser(userId, "/queue/order-updates", message);
    }
}
