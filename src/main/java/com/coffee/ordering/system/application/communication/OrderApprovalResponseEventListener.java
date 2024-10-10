package com.coffee.ordering.system.application.communication;

import com.coffee.ordering.system.application.model.OrderApprovalResponse;

public interface OrderApprovalResponseEventListener {

    void orderApproved(OrderApprovalResponse orderApprovalResponse);

    void orderRejected(OrderApprovalResponse orderApprovalResponse);
}
