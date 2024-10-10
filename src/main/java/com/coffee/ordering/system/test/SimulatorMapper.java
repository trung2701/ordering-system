package com.coffee.ordering.system.test;

import com.coffee.ordering.system.connectors.kafka.model.OrderApprovalRequestEventModel;
import com.coffee.ordering.system.connectors.kafka.model.OrderApprovalResponseEventModel;
import com.coffee.ordering.system.connectors.kafka.model.PaymentRequestEventModel;
import com.coffee.ordering.system.connectors.kafka.model.PaymentResponseEventModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class SimulatorMapper {

    public abstract PaymentResponseEventModel buildPaymentResponseEventModel(PaymentRequestEventModel messages);

    public abstract OrderApprovalResponseEventModel buildOrderApprovalResponseEventModel(OrderApprovalRequestEventModel messages);
}
