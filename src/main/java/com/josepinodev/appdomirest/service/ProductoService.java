package com.josepinodev.appdomirest.service;

import com.josepinodev.appdomirest.dto.DashboardUpdateEvent;
import com.josepinodev.appdomirest.dto.producto.ProductoDTO;
import com.josepinodev.appdomirest.mapper.ProductoMapper;
import com.josepinodev.appdomirest.model.ProductoEntity;
import com.josepinodev.appdomirest.model.UserEntity;
import com.josepinodev.appdomirest.model.userSecurity.UserDetailsImpl;
import com.josepinodev.appdomirest.repository.ProductoRepository;
import com.josepinodev.appdomirest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final UserRepository userRepository;
    private final ProductoMapper productoMapper;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    public List<ProductoDTO> findAll() {
        return productoRepository.findAll().stream()
                .map(productoMapper::toDTO)
                .toList();
    }

    public Page<ProductoDTO> findAllPaginated(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<ProductoEntity> p = productoRepository.findAll(pageRequest).stream().toList();
        System.out.println("El tamaño es: " + p.isEmpty());
        return productoRepository.findAll(pageRequest)
                .map(productoMapper::toDTO);
    }

    public Optional<ProductoDTO> findById(Long id) {
        return productoRepository.findById(id)
                .map(productoMapper::toDTO);
    }

    public Optional<ProductoDTO> findByCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo)
                .map(productoMapper::toDTO);
    }

    public List<ProductoDTO> findByActivo(Boolean activo) {
        return productoRepository.findByActivo(activo).stream()
                .map(productoMapper::toDTO)
                .toList();
    }

    public List<ProductoDTO> findByNombreContaining(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(productoMapper::toDTO)
                .toList();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public ProductoDTO save(ProductoDTO dto) {
        if (dto.getCodigo() == null || dto.getCodigo().isBlank()) {
            dto.setCodigo(generarCodigo());
        }

        ProductoEntity entity = productoMapper.toEntity(dto);

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();

            UserEntity usuario = userRepository.findById(userDetails.getId())
                    .orElse(null);

            if (usuario != null) {
                entity.setUsuarioCreador(usuario);
                log.info("Producto creado por usuario: {}", usuario.getUsername());
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener el usuario autenticado: {}", e.getMessage());
        }

        ProductoDTO saved = productoMapper.toDTO(productoRepository.save(entity));
        eventPublisher.publishEvent(new DashboardUpdateEvent(this));
        auditService.log("CREAR_PRODUCTO", "Producto #" + saved.getId() + ": " + saved.getNombre() + " creado");
        return saved;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public ProductoDTO update(ProductoDTO dto) {
        ProductoEntity existing = productoRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        dto.setFechaCreacion(existing.getFechaCreacion());
        dto.setUsuarioCreadorId(existing.getUsuarioCreador() != null ? existing.getUsuarioCreador().getId() : null);
        dto.setCodigo(existing.getCodigo());

        ProductoEntity entity = productoMapper.toEntity(dto);
        entity.setId(existing.getId());
        entity.setUsuarioCreador(existing.getUsuarioCreador());
        ProductoDTO updated = productoMapper.toDTO(productoRepository.save(entity));
        eventPublisher.publishEvent(new DashboardUpdateEvent(this));
        auditService.log("EDITAR_PRODUCTO", "Producto #" + updated.getId() + ": " + updated.getNombre() + " actualizado");
        return updated;
    }

    private String generarCodigo() {
        String ultimo = productoRepository.findTopByOrderByCodigoDesc()
                .map(ProductoEntity::getCodigo)
                .orElse("PROD-0000");
        int num = Integer.parseInt(ultimo.replace("PROD-", "")) + 1;
        return "PROD-" + String.format("%04d", num);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(Long id) {
        productoRepository.findById(id).ifPresent(producto -> {
            producto.setActivo(false);
            productoRepository.save(producto);
            eventPublisher.publishEvent(new DashboardUpdateEvent(this));
            auditService.log("ELIMINAR_PRODUCTO", "Producto #" + id + ": " + producto.getNombre() + " eliminado (soft-delete)");
        });
    }

    public Long countActive() {
        return productoRepository.countByActivo(true);
    }

    public List<ProductoDTO> findEnPromocion() {
        return productoRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getEnPromocion()))
                .map(productoMapper::toDTO)
                .toList();
    }

    public List<ProductoDTO> findAllActive() {
        return productoRepository.findByActivo(true).stream()
                .map(productoMapper::toDTO)
                .toList();
    }

    public List<ProductoDTO> searchByNombre(String query) {
        return productoRepository.findByNombreContainingIgnoreCase(query).stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .map(productoMapper::toDTO)
                .toList();
    }
}