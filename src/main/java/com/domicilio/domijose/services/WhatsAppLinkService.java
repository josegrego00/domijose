package com.domicilio.domijose.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.domicilio.domijose.dto.OrderDTO;
import com.domicilio.domijose.dto.OrderItemDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppLinkService {

    @Value("${admin.whatsapp.number}")
    private String adminPhoneNumber;
    
    @Value("${base.url}")
    private String baseUrl;

    /**
     * Genera link wa.me para que el ADMIN envíe el mensaje manualmente
     */
    public String generarLinkParaAdmin(OrderDTO order) {
        String mensaje = String.format(
            "🍕 *NUEVO PEDIDO* 🍔%n%n" +
            "*Pedido #%d*%n" +
            "*Cliente:* %s%n" +
            "*Teléfono:* %s%n" +
            "*Método de pago:* %s%n%n" +
            "*Productos:*%n%s%n" +
            "*Total:* $%.2f%n%n" +
            "📋 *Para gestionar:* %s/admin/pedidos/%d",
            order.getId(),
            order.getUserFullName(),
            order.getUserPhone(),
            order.getMetodoPago(),
            order.getItems(),
            order.getTotalAmount(),
            baseUrl,
            order.getId()
        );
        
        return generarWhatsAppLink(adminPhoneNumber, mensaje);
    }
    
    /**
     * Genera link wa.me para notificar al CLIENTE
     */
    public String generarLinkParaCliente(OrderDTO order) {
        String mensaje = String.format(
            "✅ *Pedido #%d confirmado!*%n%n" +
            "Total: $%.2f%n" +
            "Método de pago: %s%n%n" +
            "📦 *Sigue tu pedido:*%n" +
            "%s/pedidos/%d%n%n" +
            "Gracias por tu compra 🍕",
            order.getId(),
            order.getTotalAmount(),
            order.getMetodoPago(),
            baseUrl,
            order.getId()
        );
        
        return generarWhatsAppLink(order.getUserPhone(), mensaje);
    }
    
    private String formatearItems(List<OrderItemDTO> items) {
        StringBuilder sb = new StringBuilder();
        for (OrderItemDTO item : items) {
            sb.append(String.format("• %d x %s = $%.2f%n", 
                item.getQuantity(), 
                item.getProductName(), 
                item.getSubtotal()));
        }
        return sb.toString();
    }
    
    private String generarWhatsAppLink(String phoneNumber, String mensaje) {
        // Limpiar número (sin +, sin espacios, sin guiones)
        String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
        // Codificar mensaje para URL
        String encodedMessage = mensaje.replace(" ", "%20")
                                        .replace("\n", "%0A")
                                        .replace("#", "%23");
        return String.format("https://wa.me/%s?text=%s", cleanPhone, encodedMessage);
    }
}