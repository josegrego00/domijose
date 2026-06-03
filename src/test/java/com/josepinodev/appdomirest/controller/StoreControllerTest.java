package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.dto.producto.ProductoDTO;
import com.josepinodev.appdomirest.model.userSecurity.UserDetailsImpl;
import com.josepinodev.appdomirest.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StoreController.class)
class StoreControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ProductoService productoService;

    private final UserDetailsImpl user = new UserDetailsImpl(1L, "testuser", "pass",
            List.of(new SimpleGrantedAuthority("USER")), true);

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void index_WithPromotions_ShowsIndex() throws Exception {
        ProductoDTO p = new ProductoDTO();
        p.setId(1L);
        p.setNombre("Promo");
        when(productoService.findEnPromocion()).thenReturn(List.of(p));

        mockMvc.perform(get("/").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("productosDestacados"));
    }

    @Test
    void index_WithoutPromotions_ShowsRecent() throws Exception {
        ProductoDTO p = new ProductoDTO();
        p.setId(1L);
        p.setNombre("Recent");
        when(productoService.findEnPromocion()).thenReturn(List.of());
        when(productoService.findAllActive()).thenReturn(List.of(p));

        mockMvc.perform(get("/").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("productosDestacados"));
    }

    @Test
    void catalogo_WithDefaultPagination_ReturnsPage() throws Exception {
        ProductoDTO p = new ProductoDTO();
        p.setId(1L);
        p.setNombre("Test");
        Page<ProductoDTO> page = new PageImpl<>(List.of(p));
        when(productoService.findAllPaginated(0, 12)).thenReturn(page);

        mockMvc.perform(get("/catalogo").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("store/catalogo"))
                .andExpect(model().attributeExists("productos"))
                .andExpect(model().attribute("currentPage", 0));
    }

    @Test
    void catalogo_WithCustomPagination_ReturnsPage() throws Exception {
        ProductoDTO p = new ProductoDTO();
        p.setId(2L);
        p.setNombre("Test2");
        Page<ProductoDTO> page = new PageImpl<>(List.of(p));
        when(productoService.findAllPaginated(1, 5)).thenReturn(page);

        mockMvc.perform(get("/catalogo").with(authentication(auth())).param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("store/catalogo"))
                .andExpect(model().attribute("currentPage", 1));
    }

    @Test
    void productoDetalle_WhenExists_ReturnsDetail() throws Exception {
        ProductoDTO p = new ProductoDTO();
        p.setId(1L);
        p.setNombre("Producto Test");
        when(productoService.findById(1L)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/producto/{id}", 1L).with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("store/producto-detalle"))
                .andExpect(model().attributeExists("producto"));
    }

    @Test
    void productoDetalle_WhenNotExists_ThrowsException() {
        when(productoService.findById(99L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
            mockMvc.perform(get("/producto/{id}", 99L).with(authentication(auth())))
        );
    }

    @Test
    void buscar_WithResults_ReturnsResults() throws Exception {
        ProductoDTO p = new ProductoDTO();
        p.setId(1L);
        p.setNombre("Test");
        when(productoService.searchByNombre("Test")).thenReturn(List.of(p));

        mockMvc.perform(get("/buscar").with(authentication(auth())).param("q", "Test"))
                .andExpect(status().isOk())
                .andExpect(view().name("store/buscar"))
                .andExpect(model().attributeExists("resultados"))
                .andExpect(model().attribute("query", "Test"));
    }

    @Test
    void buscar_WithoutResults_ReturnsEmpty() throws Exception {
        when(productoService.searchByNombre("NoExiste")).thenReturn(List.of());

        mockMvc.perform(get("/buscar").with(authentication(auth())).param("q", "NoExiste"))
                .andExpect(status().isOk())
                .andExpect(view().name("store/buscar"))
                .andExpect(model().attribute("resultados", List.of()));
    }
}
