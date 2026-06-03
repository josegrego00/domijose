package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.dto.carrito.CarritoDTO;
import com.josepinodev.appdomirest.dto.carrito.ItemCarritoDTO;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarritoController.class)
class CarritoControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private CarritoService carritoService;
    @MockitoBean private ProductoService productoService;
    @MockitoBean private VentaService ventaService;
    @MockitoBean private RutaDomicilioService rutaDomicilioService;
    @MockitoBean private CuentaBancoService cuentaBancoService;

    private final UserDetailsImpl user = new UserDetailsImpl(1L, "testuser", "pass",
            List.of(new SimpleGrantedAuthority("USER")), true);

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void showCarrito_ReturnsCartView() throws Exception {
        CarritoDTO carrito = new CarritoDTO();
        when(carritoService.getCarrito()).thenReturn(carrito);
        when(rutaDomicilioService.findAllActive()).thenReturn(List.of());
        when(cuentaBancoService.findAllActive()).thenReturn(List.of());

        mockMvc.perform(get("/carrito").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("store/carrito"))
                .andExpect(model().attributeExists("carrito", "rutas", "cuentasBanco"));
    }

    @Test
    void agregarProducto_WhenValid_ReturnsOk() throws Exception {
        ProductoDTO producto = new ProductoDTO();
        producto.setId(1L);
        producto.setNombre("Test");

        when(productoService.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(carritoService).agregarProducto(any(), any(), any());

        mockMvc.perform(post("/carrito/agregar")
                        .with(authentication(auth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":1,\"cantidad\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    void agregarProducto_WithoutAuth_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/carrito/agregar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":1,\"cantidad\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void actualizarCantidad_WithPositiveAmount_Updates() throws Exception {
        mockMvc.perform(post("/carrito/actualizar")
                        .with(authentication(auth()))
                        .with(csrf())
                        .param("productoId", "1")
                        .param("cantidad", "3"))
                .andExpect(status().isOk());
        verify(carritoService).actualizarCantidad(1L, new BigDecimal("3"));
    }

    @Test
    void actualizarCantidad_WithZero_RemovesItem() throws Exception {
        mockMvc.perform(post("/carrito/actualizar")
                        .with(authentication(auth()))
                        .with(csrf())
                        .param("productoId", "1")
                        .param("cantidad", "0"))
                .andExpect(status().isOk());
        verify(carritoService).removerProducto(1L);
    }

    @Test
    void removerProducto_RedirectsToCart() throws Exception {
        mockMvc.perform(post("/carrito/remover/{productoId}", 1L)
                        .with(authentication(auth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/carrito"));
        verify(carritoService).removerProducto(1L);
    }

    @Test
    void limpiarCarrito_RedirectsToCart() throws Exception {
        mockMvc.perform(post("/carrito/limpiar")
                        .with(authentication(auth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/carrito"));
        verify(carritoService).limpiarCarrito();
    }

    @Test
    void checkout_WithValidData_RedirectsToConfirmado() throws Exception {
        CarritoDTO carrito = new CarritoDTO();
        carrito.setItems(List.of(new ItemCarritoDTO(1L, "Test", BigDecimal.TEN, null, BigDecimal.ONE)));
        carrito.recalcularTotal();
        VentaDTO venta = new VentaDTO();
        venta.setId(1L);

        when(carritoService.isEmpty()).thenReturn(false);
        when(carritoService.getCarrito()).thenReturn(carrito);
        when(ventaService.crearVenta(any(), anyLong(), any(), any(), any(), any(), any(), any())).thenReturn(venta);

        mockMvc.perform(post("/carrito/checkout")
                        .with(authentication(auth()))
                        .with(csrf())
                        .param("metodoPago", "EFECTIVO")
                        .param("efectivoRecibido", "10000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pedido/1/confirmado"));

        verify(carritoService).limpiarCarrito();
    }

    @Test
    void checkout_WithEmptyCart_ReturnsCartWithError() throws Exception {
        when(carritoService.isEmpty()).thenReturn(true);
        CarritoDTO carrito = new CarritoDTO();
        when(carritoService.getCarrito()).thenReturn(carrito);
        when(rutaDomicilioService.findAllActive()).thenReturn(List.of());
        when(cuentaBancoService.findAllActive()).thenReturn(List.of());

        mockMvc.perform(post("/carrito/checkout")
                        .with(authentication(auth()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("store/carrito"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void checkout_WhenServiceThrows_ReturnsCartWithError() throws Exception {
        CarritoDTO carrito = new CarritoDTO();
        carrito.setItems(List.of(new ItemCarritoDTO(1L, "Test", BigDecimal.TEN, null, BigDecimal.ONE)));
        carrito.recalcularTotal();

        when(carritoService.isEmpty()).thenReturn(false);
        when(carritoService.getCarrito()).thenReturn(carrito);
        when(ventaService.crearVenta(any(), anyLong(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Error de prueba"));
        when(rutaDomicilioService.findAllActive()).thenReturn(List.of());
        when(cuentaBancoService.findAllActive()).thenReturn(List.of());

        mockMvc.perform(post("/carrito/checkout")
                        .with(authentication(auth()))
                        .with(csrf())
                        .param("metodoPago", "EFECTIVO")
                        .param("efectivoRecibido", "10000"))
                .andExpect(status().isOk())
                .andExpect(view().name("store/carrito"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void getCarritoCount_ReturnsCount() throws Exception {
        when(carritoService.getCantidadItems()).thenReturn(new BigDecimal("3"));

        mockMvc.perform(get("/carrito/count").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }
}
