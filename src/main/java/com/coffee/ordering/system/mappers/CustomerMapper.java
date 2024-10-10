package com.coffee.ordering.system.mappers;

import com.coffee.ordering.system.connectors.kafka.model.CustomerEventModel;
import com.coffee.ordering.system.dataaccess.entity.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class CustomerMapper {

    @Mapping(target = "id", source = "customerId")
    public abstract CustomerEntity buildCustomerEntity(CustomerEventModel customerEventModel);
}
