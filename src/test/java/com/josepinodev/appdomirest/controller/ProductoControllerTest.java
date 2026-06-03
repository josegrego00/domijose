package com.josepinodev.appdomirest.controller;

import com.josepinodev.appdomirest.dto.producto.ProductoDTO;
import com.josepinodev.appdomirest.model.userSecurity.UserDetailsImpl;
import com.josepinodev.appdomirest.service.ImageService;
import com.josepinodev.appdomirest.service.ProductoService;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ProductoService productoService;
    @MockitoBean private ImageService imageService;
    @MockitoBean private VentaService ventaService;

    private final UserDetailsImpl user = new UserDetailsImpl(1L, "admin", "pass",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")), true);

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void listarProductos_ReturnsView() throws Exception {
        Page<ProductoDTO> page = new PageImpl<>(List.of(new ProductoDTO()));
        when(productoService.findAllPaginated(0, 10)).thenReturn(page);
        when(ventaService.countPendientes()).thenReturn(3L);

        mockMvc.perform(get("/admin/productos").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/productos-list"))
                .andExpect(model().attributeExists("productos"))
                .andExpect(model().attribute("cantidadPendientes", 3L));
    }

    @Test
    void nuevoProducto_ReturnsForm() throws Exception {
        when(ventaService.countPendientes()).thenReturn(0L);

        mockMvc.perform(get("/admin/productos/nuevo").with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/producto/productos-form"))
                .andExpect(model().attributeExists("producto"));
    }

    @Test
    void guardarProducto_WithoutImage_Redirects() throws Exception {
        ProductoDTO dto = new ProductoDTO();
        dto.setNombre("Nuevo Producto");
        when(productoService.save(any(ProductoDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/admin/productos")
                        .with(authentication(auth()))
                        .with(csrf())
                        .param("nombre", "Nuevo Producto"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/productos?success"));
    }

    @Test
    void guardarProducto_WithImage_SavesImage() throws Exception {
        when(imageService.saveImage(any())).thenReturn("/imagenes/productos/test.jpg");
        ProductoDTO dto = new ProductoDTO();
        dto.setNombre("Nuevo Producto");
        when(productoService.save(any(ProductoDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/admin/productos")
                        .with(authentication(auth()))
                        .with(csrf())
                        .param("nombre", "Nuevo Producto"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/productos?success"));
    }

    @Test
    void editarProducto_WhenExists_ReturnsForm() throws Exception {
        ProductoDTO p = new ProductoDTO();
        p.setId(1L);
        p.setNombre("Test");
        when(productoService.findById(1L)).thenReturn(Optional.of(p));
        when(ventaService.countPendientes()).thenReturn(0L);

        mockMvc.perform(get("/admin/productos/{id}/editar", 1L).with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/producto/productos-form"))
                .andExpect(model().attributeExists("producto"));
    }

    @Test
    void editarProducto_WhenNotExists_ThrowsException() {
        when(productoService.findById(99L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
            mockMvc.perform(get("/admin/productos/{id}/editar", 99L).with(authentication(auth())))
        );
    }

    @Test
    void actualizarProducto_WhenExists_Redirects() throws Exception {
        ProductoDTO existing = new ProductoDTO();
        existing.setId(1L);
        existing.setNombre("Old");
        existing.setImagenUrl("/old.jpg");

        when(productoService.findById(1L)).thenReturn(Optional.of(existing));
        when(productoService.update(any(ProductoDTO.class))).thenReturn(existing);

        mockMvc.perform(post("/admin/productos/{id}", 1L)
                        .with(authentication(auth()))
                        .with(csrf())
                        .param("nombre", "Updated"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/productos?updated"));
    }

    @Test
    void eliminarProducto_Redirects() throws Exception {
        mockMvc.perform(post("/admin/productos/{id}/eliminar", 1L)
                        .with(authentication(auth()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/productos?deleted"));

        verify(productoService).delete(1L);
    }
}
