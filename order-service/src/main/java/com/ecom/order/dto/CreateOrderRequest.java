package com.ecom.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {

    @Email
    private String customerEmail;

    @NotEmpty
    private List<@Valid OrderItemRequest> items;
}
