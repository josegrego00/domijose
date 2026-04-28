# Resumen del Día - 26 de Abril 2026

## Completado

### 1. WhatsApp - Botón Cliente
- Eliminado "Enviar al Restaurante"
- Nuevo botón "📱 Enviar al Cliente" para admin

### 2. Badge Carrito (Header)
- CarritoInterceptor - muestra conteo real
- Solo visible para CLIENTE

### 3. Badge Pedidos Pendientes (Admin)
- AdminPedidosInterceptor - cuenta POR_CONFIRMAR
- Visible en rutas /admin/**

### 4. Filtro Fecha CLIENTE
- Default: pedidos del día actual
- Input date con botón 🔍 Buscar

### 5. Filtro Fecha + Paginación ADMIN
- Default: día actual
- 10 pedidos por página
- Navegación Anterior/Siguiente

### 6. UI/UX
- Botón "🔍 Buscar": rojo, grande, píldora
- "Ver Mi Pedido": marrón discreto
- Badge pedidos: rojo circular

### 7. Tests Corregidos
- CarritoServiceTest: cambia a ProductDTO

## Archivos

### Nuevos
- CarritoInterceptor.java
- AdminPedidosInterceptor.java
- WebConfig.java

### Modificados
- AdminOrderController.java
- OrderController.java
- OrderRepository.java
- OrderService.java
- detalle.html
- mis-pedidos.html
- lista.html
- header.html
- marketplace.css

## Git

```
ce20409 chore: trigger activity
c535f92 Merge: resolver conflictos con cambios locales
631b7d1 feat: badges, filtros fecha y paginacion en pedidos
```

## Tech Stack
- Java 21
- Spring Boot 4.0.5
- Spring JPA + MySQL
- Thymeleaf