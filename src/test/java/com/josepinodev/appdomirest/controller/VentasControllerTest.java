package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.dto.venta.DetalleVentaDTO;
import com.josepinodev.appdomirest.dto.venta.VentaDTO;
import com.josepinodev.appdomirest.model.EVentaEstado;
import com.josepinodev.appdomirest.model.userSecurity.UserDetailsImpl;
import com.josepinodev.appdomirest.service.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VentasController.class)
class VentasControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private VentaService ventaService;

    private final UserDetailsImpl user = new UserDetailsImpl(1L, "testuser", "pass",
            List.of(new SimpleGrantedAuthority("USER")), true);
    private final UserDetailsImpl admin = new UserDetailsImpl(2L, "admin", "pass",
            List.of(new SimpleGrantedAuthority("ADMIN")), true);

    private UsernamePasswordAuthenticationToken userAuth() {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private UsernamePasswordAuthenticationToken adminAuth() {
        return new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());
    }

    private VentaDTO createVentaDTO(Long id, Long userId, EVentaEstado estado) {
        VentaDTO v = new VentaDTO();
        v.setId(id);
        v.setUsuarioId(userId);
        v.setUsuarioUsername("testuser");
        v.setEstado(estado);
        v.setTotal(new BigDecimal("10000"));
        v.setSubtotal(new BigDecimal("10000"));
        v.setEsDomicilio(false);
        v.setDetalles(Collections.emptyList());
        return v;
    }

    // ---- mis-pedidos ----

    @Test
    void misPedidos_ReturnsView() throws Exception {
        VentaDTO v = createVentaDTO(1L, 1L, EVentaEstado.PENDIENTE);
        Page<VentaDTO> page = new PageImpl<>(List.of(v));
        when(ventaService.getVentasUsuario(anyLong(), any(), eq("todos"))).thenReturn(page);

        mockMvc.perform(get("/mis-pedidos").with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(view().name("store/mis-pedidos"))
                .andExpect(model().attributeExists("ventas"));
    }

    @Test
    void misPedidos_WithSuccessParam_AddsMessage() throws Exception {
        VentaDTO v = createVentaDTO(1L, 1L, EVentaEstado.PENDIENTE);
        Page<VentaDTO> page = new PageImpl<>(List.of(v));
        when(ventaService.getVentasUsuario(anyLong(), any(), eq("todos"))).thenReturn(page);

        mockMvc.perform(get("/mis-pedidos").param("success", "true").with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("successMessage"));
    }

    // ---- pedido confirmado ----

    @Test
    void pedidoConfirmado_WhenOwner_ReturnsView() throws Exception {
        VentaDTO v = createVentaDTO(1L, 1L, EVentaEstado.PENDIENTE);
        when(ventaService.getVentaById(1L)).thenReturn(v);

        mockMvc.perform(get("/pedido/{id}/confirmado", 1L).with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(view().name("store/pedido-confirmado"))
                .andExpect(model().attributeExists("venta"));
    }

    @Test
    void pedidoConfirmado_WhenNotOwner_Redirects() throws Exception {
        VentaDTO v = createVentaDTO(1L, 99L, EVentaEstado.PENDIENTE);
        when(ventaService.getVentaById(1L)).thenReturn(v);

        mockMvc.perform(get("/pedido/{id}/confirmado", 1L).with(authentication(userAuth())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mis-pedidos"));
    }

    // ---- admin ventas ----

    @Test
    void adminVentas_WithoutEstado_ReturnsAll() throws Exception {
        VentaDTO v = createVentaDTO(1L, 1L, EVentaEstado.PENDIENTE);
        Page<VentaDTO> page = new PageImpl<>(List.of(v));
        when(ventaService.getAllVentas(any())).thenReturn(page);
        when(ventaService.countPendientes()).thenReturn(5L);

        mockMvc.perform(get("/admin/ventas").with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/ventas-list"))
                .andExpect(model().attributeExists("ventas"));
    }

    @Test
    void adminVentas_WithEstado_ReturnsFiltered() throws Exception {
        VentaDTO v = createVentaDTO(1L, 1L, EVentaEstado.PENDIENTE);
        Page<VentaDTO> page = new PageImpl<>(List.of(v));
        when(ventaService.getVentasByEstado(eq(EVentaEstado.PENDIENTE), any())).thenReturn(page);
        when(ventaService.countPendientes()).thenReturn(5L);

        mockMvc.perform(get("/admin/ventas").param("estado", "PENDIENTE").with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(model().attribute("estadoFilter", "PENDIENTE"));
    }

    // ---- admin venta detalle ----

    @Test
    void ventaDetalle_ReturnsView() throws Exception {
        VentaDTO v = createVentaDTO(1L, 1L, EVentaEstado.PENDIENTE);
        when(ventaService.getVentaById(1L)).thenReturn(v);
        when(ventaService.countPendientes()).thenReturn(5L);

        mockMvc.perform(get("/admin/ventas/{id}", 1L).with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/ventas-detail"))
                .andExpect(model().attributeExists("venta"));
    }

    // ---- admin tomar pedido ----

    @Test
    void tomarPedido_Success_RedirectsWithMessage() throws Exception {
        when(ventaService.tomarPedido(1L)).thenReturn(createVentaDTO(1L, 1L, EVentaEstado.PEDIDO_TOMADO));

        mockMvc.perform(post("/admin/ventas/{id}/tomar", 1L)
                        .with(authentication(adminAuth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/ventas/1"));
    }

    @Test
    void tomarPedido_Error_RedirectsWithError() throws Exception {
        when(ventaService.tomarPedido(1L)).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/admin/ventas/{id}/tomar", 1L)
                        .with(authentication(adminAuth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/ventas/1"));
    }

    // ---- admin enviar pedido ----

    @Test
    void enviarPedido_Success_RedirectsWithMessage() throws Exception {
        when(ventaService.enviarPedido(1L)).thenReturn(createVentaDTO(1L, 1L, EVentaEstado.ENVIADO));

        mockMvc.perform(post("/admin/ventas/{id}/enviar", 1L)
                        .with(authentication(adminAuth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/ventas/1"));
    }

    @Test
    void enviarPedido_Error_RedirectsWithError() throws Exception {
        when(ventaService.enviarPedido(1L)).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/admin/ventas/{id}/enviar", 1L)
                        .with(authentication(adminAuth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/ventas/1"));
    }

    // ---- admin completar venta ----

    @Test
    void completarVenta_Success_RedirectsWithMessage() throws Exception {
        when(ventaService.completarVenta(1L)).thenReturn(createVentaDTO(1L, 1L, EVentaEstado.COMPLETADO));

        mockMvc.perform(post("/admin/ventas/{id}/completar", 1L)
                        .with(authentication(adminAuth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/ventas/1"));
    }

    @Test
    void completarVenta_Error_RedirectsWithError() throws Exception {
        when(ventaService.completarVenta(1L)).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/admin/ventas/{id}/completar", 1L)
                        .with(authentication(adminAuth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/ventas/1"));
    }

    // ---- admin cancelar venta ----

    @Test
    void cancelarVenta_Success_RedirectsWithMessage() throws Exception {
        when(ventaService.cancelarVenta(1L)).thenReturn(createVentaDTO(1L, 1L, EVentaEstado.CANCELADA));

        mockMvc.perform(post("/admin/ventas/{id}/cancelar", 1L)
                        .with(authentication(adminAuth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/ventas/1"));
    }

    // ---- cliente cancelar pedido ----

    @Test
    void cancelarVentaCliente_Success_Redirects() throws Exception {
        when(ventaService.cancelarVentaCliente(1L, 1L)).thenReturn(createVentaDTO(1L, 1L, EVentaEstado.CANCELADA));

        mockMvc.perform(post("/pedido/{id}/cancelar", 1L)
                        .with(authentication(userAuth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mis-pedidos"));
    }

    @Test
    void cancelarVentaCliente_Error_Redirects() throws Exception {
        when(ventaService.cancelarVentaCliente(1L, 1L)).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/pedido/{id}/cancelar", 1L)
                        .with(authentication(userAuth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mis-pedidos"));
    }

    // ---- count pendientes ----

    @Test
    void countPendientes_ReturnsCount() throws Exception {
        when(ventaService.countPendientes()).thenReturn(3L);

        mockMvc.perform(get("/admin/ventas/pendientes/count").with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }
}
