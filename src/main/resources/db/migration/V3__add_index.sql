ALTER TABLE order_items
    ADD CONSTRAINT "FK_ITEMS_ORDER_ID" FOREIGN KEY (order_id)
        REFERENCES orders (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID;

ALTER TABLE order_address
    ADD CONSTRAINT "FK_ADDRESS_ORDER_ID" FOREIGN KEY (order_id)
        REFERENCES orders (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
        NOT VALID;

CREATE INDEX "payment_outbox_saga_status"
    ON payment_outbox
        (type, outbox_status, saga_status);

CREATE INDEX "order_approval_outbox_saga_status"
    ON order_approval_outbox
        (type, outbox_status, saga_status);