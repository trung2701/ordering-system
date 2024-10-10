package com.coffee.ordering.system.application.communication;

import com.coffee.ordering.system.application.model.PaymentResponse;

public interface PaymentResponseEventListener {

    void paymentCompleted(PaymentResponse paymentResponse);

    void paymentCancelled(PaymentResponse paymentResponse);
}
