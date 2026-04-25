package com.domicilio.domijose.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class OrderConfirmadoDTO {
    private OrderDTO order;
    private String adminWhatsAppLink;
    private String clienteWhatsAppLink;
}