package com.coffee.ordering.system.mappers;

import com.coffee.ordering.system.application.outbox.model.approval.OrderApprovalOutboxMessage;
import com.coffee.ordering.system.application.outbox.model.payment.OrderPaymentOutboxMessage;
import com.coffee.ordering.system.dataaccess.entity.OrderApprovalOutboxEntity;
import com.coffee.ordering.system.dataaccess.entity.PaymentOrderOutboxEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class OutboxMapper {

    public abstract OrderApprovalOutboxEntity buildOrderApprovalOutboxEntity(OrderApprovalOutboxMessage orderApprovalOutboxMessage);

    public abstract OrderApprovalOutboxMessage buildOrderApprovalOutboxMessage(OrderApprovalOutboxEntity approvalOutboxEntity);

    public abstract OrderPaymentOutboxMessage buildOrderPaymentOutboxMessage(PaymentOrderOutboxEntity paymentOrderOutboxEntity);

    public abstract PaymentOrderOutboxEntity buildPaymentOutboxEntity(OrderPaymentOutboxMessage orderPaymentOutboxMessage);
}
