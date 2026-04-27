package com.domicilio.domijose.controllers;

import com.domicilio.domijose.dto.OrderDTO;
import com.domicilio.domijose.dto.OrderItemDTO;
import com.domicilio.domijose.services.CarritoService;
import com.domicilio.domijose.services.security.CustomUserDetails;
import com.domicilio.domijose.services.MetodoPagoService;
import com.domicilio.domijose.services.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/carrito")
public class CarritoController {
    private static final Logger log = LoggerFactory.getLogger(CarritoController.class);

    private final CarritoService carritoService;
    private final OrderService orderService;
    private final MetodoPagoService metodoPagoService;

    public CarritoController(CarritoService carritoService,
            OrderService orderService,
            MetodoPagoService metodoPagoService) {
        this.carritoService = carritoService;
        this.orderService = orderService;
        this.metodoPagoService = metodoPagoService;
    }

    @GetMapping
    public String verCarrito(Model model, HttpSession session) {
        model.addAttribute("carrito", carritoService.obtenerCarritoParaVista(session));
        model.addAttribute("total", carritoService.obtenerTotal(session));
        model.addAttribute("cuentas", metodoPagoService.getCuentas());
        model.addAttribute("qrs", metodoPagoService.getQrs());

        return "carrito/ver";
    }

    @PostMapping("/agregar")
    public String agregarAlCarrito(@RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        boolean agregado = carritoService.agregarProducto(productId, quantity, session);

        if (agregado) {
            redirectAttributes.addFlashAttribute("success", "Producto agregado al carrito");
        } else {
            redirectAttributes.addFlashAttribute("error", "No hay suficiente stock disponible");
        }

        return "redirect:/productos";
    }

    @PostMapping("/actualizar")
    public String actualizarCarrito(@RequestParam Long productId,
            @RequestParam Integer quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        boolean actualizado = carritoService.actualizarCantidad(productId, quantity, session);

        if (actualizado) {
            redirectAttributes.addFlashAttribute("success", "Cantidad actualizada");
        } else {
            redirectAttributes.addFlashAttribute("error", "Stock insuficiente");
        }

        return "redirect:/carrito";
    }

    @PostMapping("/eliminar")
    public String eliminarDelCarrito(@RequestParam Long productId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        carritoService.eliminarProducto(productId, session);
        redirectAttributes.addFlashAttribute("success", "Producto eliminado del carrito");

        return "redirect:/carrito";
    }

    @PostMapping("/checkout")
    public String checkout(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String metodoPago,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        if (carritoService.estaVacio(session)) {
            redirectAttributes.addFlashAttribute("error", "El carrito está vacío");
            return "redirect:/carrito";
        }

        List<OrderItemDTO> items = carritoService.convertirParaPedido(session);

        if (items == null) {
            redirectAttributes.addFlashAttribute("error", "Stock insuficiente para algunos productos");
            return "redirect:/carrito";
        }

        try {
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setMetodoPago(metodoPago);
            OrderDTO order = orderService.createOrder(orderDTO, userDetails.getUser().getId(), items);
            carritoService.vaciarCarrito(session);

            redirectAttributes.addFlashAttribute("success", "Pedido creado exitosamente!");
            return "redirect:/pedidos/" + order.getId();

        } catch (Exception e) {
            log.error("Error al procesar checkout: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/carrito";
        }
    }
}