package org.microservice.order.service;

import lombok.Data;
import com.mzc.markets.common.model.OrderStatus;

import java.io.Serializable;

@Data
public class OrderStatusMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String orderId;
    private OrderStatus status;
    private Object details;
    private long timestamp;
}
