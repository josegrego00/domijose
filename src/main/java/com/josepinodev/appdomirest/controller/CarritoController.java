package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.dto.carrito.AgregarCarritoRequest;
import com.josepinodev.appdomirest.dto.carrito.CarritoDTO;
import com.josepinodev.appdomirest.dto.carrito.CheckoutRequest;
import com.josepinodev.appdomirest.dto.cuenta.CuentaBancoDTO;
import com.josepinodev.appdomirest.dto.producto.ProductoDTO;
import com.josepinodev.appdomirest.dto.ruta.RutaDomicilioDTO;
import com.josepinodev.appdomirest.dto.venta.VentaDTO;
import com.josepinodev.appdomirest.model.userSecurity.UserDetailsImpl;
import com.josepinodev.appdomirest.service.CarritoService;
import com.josepinodev.appdomirest.service.CuentaBancoService;
import com.josepinodev.appdomirest.service.ProductoService;
import com.josepinodev.appdomirest.service.RutaDomicilioService;
import com.josepinodev.appdomirest.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/carrito")
@RequiredArgsConstructor
@Slf4j
public class CarritoController {

    private final CarritoService carritoService;
    private final ProductoService productoService;
    private final VentaService ventaService;
    private final RutaDomicilioService rutaDomicilioService;
    private final CuentaBancoService cuentaBancoService;

    @GetMapping
    public String showCarrito(Model model) {
        log.debug("=== SHOW CARRITO ===");

        CarritoDTO carrito = carritoService.getCarrito();
        List<RutaDomicilioDTO> rutas = rutaDomicilioService.findAllActive();
        List<CuentaBancoDTO> cuentasBanco = cuentaBancoService.findAllActive();

        model.addAttribute("carrito", carrito);
        model.addAttribute("rutas", rutas);
        model.addAttribute("cuentasBanco", cuentasBanco);

        return "store/carrito";
    }

    @PostMapping("/agregar")
    public ResponseEntity<Void> agregarProducto(@Valid @RequestBody AgregarCarritoRequest request,
                                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.debug("=== AGREGAR PRODUCTO === productoId: {}, cantidad: {}", request.getProductoId(), request.getCantidad());

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ProductoDTO producto = productoService.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        carritoService.agregarProducto(producto, request.getCantidad(), request.getObservacion());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/actualizar")
    @ResponseBody
    public ResponseEntity<Void> actualizarCantidad(@RequestParam Long productoId,
                                                   @RequestParam BigDecimal cantidad) {
        log.debug("=== ACTUALIZAR CANTIDAD === productoId: {}, cantidad: {}", productoId, cantidad);

        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            carritoService.removerProducto(productoId);
        } else {
            carritoService.actualizarCantidad(productoId, cantidad);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/remover/{productoId}")
    public String removerProducto(@PathVariable Long productoId) {
        log.debug("=== REMOVER PRODUCTO === productoId: {}", productoId);

        carritoService.removerProducto(productoId);

        return "redirect:/carrito";
    }

    @PostMapping("/limpiar")
    public String limpiarCarrito() {
        log.debug("=== LIMPIAR CARRITO ===");

        carritoService.limpiarCarrito();

        return "redirect:/carrito";
    }

    @PostMapping("/checkout")
    public String checkout(CheckoutRequest request,
                           @AuthenticationPrincipal UserDetailsImpl userDetails,
                           Model model) {
        log.debug("=== CHECKOUT === direccion: {}, rutaId: {}, metodoPago: {}", request.getDireccion(), request.getRutaId(), request.getMetodoPago());

        if (userDetails == null) {
            return "redirect:/login";
        }

        if (carritoService.isEmpty()) {
            model.addAttribute("error", "El carrito está vacío");
            return showCarrito(model);
        }

        try {
            CarritoDTO carrito = carritoService.getCarrito();

            VentaDTO venta = ventaService.crearVenta(
                    carrito,
                    userDetails.getId(),
                    request.getDireccion(),
                    request.getRutaId(),
                    request.getEsDomicilio(),
                    request.getMetodoPago(),
                    request.getEfectivoRecibido(),
                    request.getCuentaBancoId()
            );

            carritoService.limpiarCarrito();

            log.info("Venta creada exitosamente: {}", venta.getId());

            return "redirect:/pedido/" + venta.getId() + "/confirmado";

        } catch (Exception e) {
            log.error("Error en checkout: ", e);
            model.addAttribute("error", "Error al procesar el pedido: " + e.getMessage());
            return showCarrito(model);
        }
    }

    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<BigDecimal> getCarritoCount() {
        BigDecimal count = carritoService.getCantidadItems();
        return ResponseEntity.ok(count);
    }
}
