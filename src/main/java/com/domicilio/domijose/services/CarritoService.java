package com.domicilio.domijose.services;

import com.domicilio.domijose.dto.CarritoItemDTO;
import com.domicilio.domijose.dto.OrderItemDTO;
import com.domicilio.domijose.dto.ProductDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para manejar la lógica del carrito de compras
 * El carrito se almacena en sesión como Map<Long, Integer> (productId -> cantidad)
 */
@Service
public class CarritoService {
    private static final Logger log = LoggerFactory.getLogger(CarritoService.class);
    
    private final ProductService productService;
    
    // Clave para almacenar el carrito en sesión
    private static final String CARRITO_SESSION_KEY = "carrito";
    
    public CarritoService(ProductService productService) {
        this.productService = productService;
    }
    
    /**
     * Obtiene el carrito de la sesión
     */
    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCarritoMap(HttpSession session) {
        Map<Long, Integer> carrito = (Map<Long, Integer>) session.getAttribute(CARRITO_SESSION_KEY);
        if (carrito == null) {
            carrito = new HashMap<>();
            session.setAttribute(CARRITO_SESSION_KEY, carrito);
        }
        return carrito;
    }
    
    /**
     * Guarda el carrito en la sesión
     */
    private void saveCarritoMap(HttpSession session, Map<Long, Integer> carrito) {
        session.setAttribute(CARRITO_SESSION_KEY, carrito);
    }
    
    /**
     * Agrega un producto al carrito
     * @return true si se agregó correctamente, false si hay error de stock
     */
    public boolean agregarProducto(Long productId, Integer cantidad, HttpSession session) {
        log.info("Agregando producto {} al carrito, cantidad: {}", productId, cantidad);
        
        try {
            // Validar que el producto existe y tiene stock
            ProductDTO producto = productService.getProductByIdForPublic(productId);
            
            if (producto.getStock() < cantidad) {
                log.warn("Stock insuficiente para producto {}. Stock disponible: {}", productId, producto.getStock());
                return false;
            }
            
            Map<Long, Integer> carrito = getCarritoMap(session);
            int nuevaCantidad = carrito.getOrDefault(productId, 0) + cantidad;
            
            // Validar stock total después de agregar
            if (producto.getStock() < nuevaCantidad) {
                log.warn("Stock total excedido para producto {}. Máximo: {}", productId, producto.getStock());
                return false;
            }
            
            carrito.put(productId, nuevaCantidad);
            saveCarritoMap(session, carrito);
            log.info("Producto {} agregado correctamente. Nueva cantidad: {}", productId, nuevaCantidad);
            return true;
            
        } catch (IllegalArgumentException e) {
            log.error("Producto no encontrado: {}", productId);
            return false;
        }
    }
    
    /**
     * Actualiza la cantidad de un producto en el carrito
     * @return true si se actualizó correctamente, false si hay error
     */
    public boolean actualizarCantidad(Long productId, Integer cantidad, HttpSession session) {
        log.info("Actualizando producto {} a cantidad: {}", productId, cantidad);
        
        Map<Long, Integer> carrito = getCarritoMap(session);
        
        if (!carrito.containsKey(productId)) {
            log.warn("Producto {} no existe en el carrito", productId);
            return false;
        }
        
        if (cantidad <= 0) {
            carrito.remove(productId);
            log.info("Producto {} eliminado del carrito", productId);
        } else {
            // Validar stock disponible
            try {
                ProductDTO producto = productService.getProductByIdForPublic(productId);
                if (producto.getStock() < cantidad) {
                    log.warn("Stock insuficiente para producto {}. Stock: {}, solicitado: {}", 
                             productId, producto.getStock(), cantidad);
                    return false;
                }
                carrito.put(productId, cantidad);
                log.info("Producto {} actualizado a cantidad: {}", productId, cantidad);
            } catch (IllegalArgumentException e) {
                log.error("Producto no encontrado: {}", productId);
                carrito.remove(productId);
                return false;
            }
        }
        
        saveCarritoMap(session, carrito);
        return true;
    }
    
    /**
     * Elimina un producto del carrito
     */
    public void eliminarProducto(Long productId, HttpSession session) {
        log.info("Eliminando producto {} del carrito", productId);
        
        Map<Long, Integer> carrito = getCarritoMap(session);
        carrito.remove(productId);
        saveCarritoMap(session, carrito);
    }
    
    /**
     * Obtiene el carrito como lista de CarritoItemDTO para mostrar en la vista
     */
    public List<CarritoItemDTO> obtenerCarritoParaVista(HttpSession session) {
        Map<Long, Integer> carritoMap = getCarritoMap(session);
        List<CarritoItemDTO> items = new ArrayList<>();
        
        for (Map.Entry<Long, Integer> entry : carritoMap.entrySet()) {
            try {
                ProductDTO producto = productService.getProductByIdForPublic(entry.getKey());
                CarritoItemDTO item = new CarritoItemDTO(
                    producto.getId(),
                    producto.getName(),
                    producto.getImageUrl(),
                    producto.getPrice(),
                    entry.getValue()
                );
                items.add(item);
            } catch (IllegalArgumentException e) {
                log.warn("Producto no disponible en carrito, eliminando: {}", entry.getKey());
                carritoMap.remove(entry.getKey());
            }
        }
        
        return items;
    }
    
    /**
     * Obtiene el total del carrito
     */
    public BigDecimal obtenerTotal(HttpSession session) {
        List<CarritoItemDTO> items = obtenerCarritoParaVista(session);
        return items.stream()
                .map(CarritoItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Convierte el carrito a lista de OrderItemDTO para crear el pedido
     * También valida stock de todos los productos
     * @return Lista de OrderItemDTO o null si hay error de stock
     */
    public List<OrderItemDTO> convertirParaPedido(HttpSession session) {
        Map<Long, Integer> carritoMap = getCarritoMap(session);
        List<OrderItemDTO> items = new ArrayList<>();
        
        for (Map.Entry<Long, Integer> entry : carritoMap.entrySet()) {
            try {
                ProductDTO producto = productService.getProductByIdForPublic(entry.getKey());
                
                // Validar stock antes de crear pedido
                if (producto.getStock() < entry.getValue()) {
                    log.error("Stock insuficiente para producto {}: disponible {}, solicitado {}", 
                             producto.getName(), producto.getStock(), entry.getValue());
                    return null;
                }
                
                OrderItemDTO item = new OrderItemDTO();
                item.setProductId(entry.getKey());
                item.setQuantity(entry.getValue());
                items.add(item);
                
            } catch (IllegalArgumentException e) {
                log.error("Producto no encontrado: {}", entry.getKey());
                return null;
            }
        }
        
        return items;
    }
    
    /**
     * Vacía el carrito después de crear un pedido
     */
    public void vaciarCarrito(HttpSession session) {
        log.info("Vaciar carrito");
        session.removeAttribute(CARRITO_SESSION_KEY);
    }
    
    /**
     * Verifica si el carrito está vacío
     */
    public boolean estaVacio(HttpSession session) {
        Map<Long, Integer> carrito = getCarritoMap(session);
        return carrito.isEmpty();
    }
    
    /**
     * Obtiene el número de items en el carrito (para el badge)
     */
    public int getCantidadItems(HttpSession session) {
        Map<Long, Integer> carrito = getCarritoMap(session);
        return carrito.values().stream().mapToInt(Integer::intValue).sum();
    }
}