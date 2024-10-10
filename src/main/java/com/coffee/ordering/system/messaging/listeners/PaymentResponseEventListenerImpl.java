package com.coffee.ordering.system.messaging.listeners;

import com.coffee.ordering.system.application.communication.PaymentResponseEventListener;
import com.coffee.ordering.system.application.model.PaymentResponse;
import com.coffee.ordering.system.application.saga.OrderPaymentSaga;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static com.coffee.ordering.system.common.CoffeeConstants.FAILURE_MESSAGE_DELIMITER;

@Slf4j
@Validated
@Service
public class PaymentResponseEventListenerImpl implements PaymentResponseEventListener {

    private final OrderPaymentSaga orderPaymentSaga;

    public PaymentResponseEventListenerImpl(OrderPaymentSaga orderPaymentSaga) {
        this.orderPaymentSaga = orderPaymentSaga;
    }

    @Override
    public void paymentCompleted(PaymentResponse paymentResponse) {
        orderPaymentSaga.process(paymentResponse);
        log.info("Order Payment Saga process operation is completed for order id: {}", paymentResponse.getOrderId());
    }

    @Override
    public void paymentCancelled(PaymentResponse paymentResponse) {
        orderPaymentSaga.rollback(paymentResponse);
        log.info("Order is roll backed for order id: {} with failure messages: {}", paymentResponse.getOrderId(),
                String.join(FAILURE_MESSAGE_DELIMITER, paymentResponse.getFailureMessages()));
    }
}
