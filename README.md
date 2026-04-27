# DomiJose - Sistema de Pedidos a Domicilio

Aplicación web para gestión de pedidos a domicilio (delivery) desarrollada con Spring Boot.

## 🚀 Características

- **Gestión de productos**: CRUD completo de productos con upload de imágenes
- **Gestión de pedidos**: Estados completos POR_CONFIRMAR → CONFIRMADO → EN_PREPARACION → EN_CAMINO → ENTREGADO
- **Métodos de pago**: Admin puede configurar cuentas bancarias y códigos QR
- **Opciones de pago**: Cliente selecciona EFECTIVO, TRANSFERENCIA o DATAFONO
- **Notificaciones WhatsApp**: Botón para enviar al cliente vía wa.me
- **Autenticación**: Registro e inicio de sesión con Spring Security
- **Roles**: CLIENTE (hacer pedidos) y ADMIN (gestionar menú y pedidos)
- **Diseño Mobile-First**: Interfaz responsiva con menú hamburguesa
- **Estilo Minimalista**: CSS personalizado con paleta verde manzana
- **Logging**: Registro de sesiones de usuarios
- **Badge Carrito**: Conteo real de productos en el header (solo CLIENTE)
- **Badge Pedidos Pendientes**: Conteo de pedidos POR_CONFIRMAR en panel admin
- **Filtro por fecha**: Vista de pedidos filtrada por día
- **Paginación**: 10 pedidos por página en admin

## 🛠️ Tech Stack

- Java 21
- Spring Boot 4.0.5
- Spring JPA + MySQL
- Spring Security (BCrypt)
- Thymeleaf
- Lombok
- Maven

## 📋 Requisitos

- JDK 21
- MySQL 8.0+
- Maven 3.9+

## ⚙️ Configuración

1. Clonar el repositorio:
```bash
git clone https://github.com/josegrego00/domijose.git
```

2. Configurar base de datos en `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/domijose
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
```

3. Ejecutar:
```bash
./mvnw spring-boot:run
```

4. Acceder a: http://localhost:8080

## 📁 Estructura

```
src/main/java/com/domicilio/domijose/
├── config/          # Configuración de Spring
├── controllers/     # Controllers Thymeleaf
├── dto/             # Data Transfer Objects
├── mappers/         # MapStruct (opcional)
├── models/          # Entidades JPA
├── repositories/    # Repositorios JPA
└── services/        # Lógica de negocio
```

## 📝 Licencia

MIT License