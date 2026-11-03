package org.microservice.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderConsumerService {

    @JmsListener(destination = "order-submit-queue")
    public void receiveOrderFromQueue(String message) {
        log.info("Received order from queue: {}", message);
        // Process order here
        // You can update database, call external services, etc.
    }

    @JmsListener(destination = "order-published-topic", containerFactory = "topicListenerFactory")
    public void receiveOrderFromTopic(String message) {
        log.info("Received order update from topic: {}", message);
        // Subscribe to order updates
    }
}
