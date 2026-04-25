# AGENTS.md

## Visión General del Proyecto
- Aplicación Spring Boot 4.0.5 (Java 21, Maven)
- Proyecto simple de un solo módulo
- Sistema de pedidos a domicilio (delivery)
- Roles: CLIENTE (ver/hacer pedidos), ADMIN (gestionar menú y estados)
- Stack: Spring JPA + MySQL + Spring Web + Thymeleaf

## Comandos
- Ejecutar: `./mvnw spring-boot:run` (usa el wrapper de Maven)
- Compilar: `./mvnw package`
- Tests: `./mvnw test`

## Requisitos
- Base de datos MySQL requerida en ejecución (dependencia mysql-connector-j)
- Configurar conexión a la base de datos en `application.properties`

## Notas
- Punto de entrada: `com.domicilio.domijose.DomijoseApplication`
- Usa Lombok - procesamiento de anotaciones habilitado en maven-compiler-plugin
- **Usar MapStruct** para mapeos entre entidades y DTOs
- **Usar SLF4J Logger** en todos los services (`LoggerFactory.getLogger(...)`)
- **Solo un DTO por entidad** (sirve para request y response)
- Estructura estándar de proyecto Spring Boot

---

## Diseño UI/UX (Mobile-First)

### Enfoque
- **Mobile-first**: las vistas están diseñadas para móvil
- Estilo minimalista, sin frameworks CSS pesados
- CSS personalizado en `static/css/`

### Recursos Estáticos
```
src/main/resources/
├── static/
│   ├── css/
│   │   ├── login.css
│   │   ├── registro.css
│   │   └── marketplace.css
│   ├── js/
│   └── images/
│       └── productos/    ← Imágenes de productos
└── templates/
    ├── index.html
    ├── login.html
    ├── registro.html
    ├── fragmentos/
    │   ├── header.html
    │   ├── footer.html
    │   └── product-card.html
    ├── productos/
    │   ├── lista.html
    │   └── detalle.html
    └── admin/
        └── productos/
            ├── lista.html
            └── form.html
```

### Estilos CSS
- Viewport: `maximum-scale=1.0, user-scalable=no` (evita zoom automático)
- Ancho: 100%, max-width 400px (formularios), grid responsivo
- Font-size: 16px mínimo (evita zoom en iOS)
- Padding touch: 14px-16px para botones inputs
- Border-radius: 12px (inputs), 50px (botones píldora)

### Paleta de Colores
- Verde principal: `#7ED957` (verde manzana)
- Verde oscuro hover: `#5DBB4D`
- Fondo degradado: `#F0FFF4` → `#FFFFFF`
- Texto verde oscuro: `#2E7D32`

---

## Arquitectura del Proyecto

### Estructura de Paquetes
```
src/main/java/com/domicilio/domijose/
├── models/           # Entidades JPA (User, Product, Order, OrderItem, enums)
├── repositories/     # interfaces JPA Repository
├── services/         # lógica de negocio (usar SLF4J Logger)
├── controllers/      # Controllers Thymeleaf
├── mappers/         # MapStruct para mapeo entidades <-> DTOs
├── config/           # configuración Spring (Security, etc.)
└── dto/              # Data Transfer Objects
```

### Entidades y Relaciones JPA
- **User**: id, email, password (BCrypt), fullName, phone, role (CLIENTE/ADMIN), orders (OneToMany)
- **Product**: id, name, description, price, stock, imageUrl, available, category, createdAt
- **Order**: id, orderDate, status, totalAmount, user (ManyToOne), items (OneToMany), metodoPago
- **OrderItem**: id, quantity, unitPrice (snapshot), subtotal, order (ManyToOne), product (ManyToOne)
- **MetodoPago**: id, banco, tipoCuenta, numeroCuenta, nombreTitular, qrUrl, activo

### Estados del Pedido
| Estado | Cuándo cambia |
|--------|--------------|
| POR_CONFIRMAR | Cliente selecciona método de pago |
| CONFIRMADO | Admin confirma el pedido |
| EN_PREPARACION | Admin envía al restaurante |
| EN_CAMINO | Domiciliario lleva el pedido |
| ENTREGADO | Cliente confirma recepción |
| CANCELADO | Cliente cancela (solo si POR_CONFIRMAR) |

### Validaciones
- **Entidades**: sin validaciones JSR-303
- **DTOs**: con validaciones (@NotBlank, @Positive, etc.) y @Valid en controllers

### Service - Validaciones de Negocio
- **ProductService.saveProduct**: valida que no exista producto con el mismo nombre (existsByNameIgnoreCase)
- **ProductService.updateProduct**: valida que no exista otro producto con ese nombre

---

## Seguridad (Spring Security)

### Configuración Requerida
- Authentication con BCrypt password encoding
- Authorization basada en roles: CLIENTE vs ADMIN
- Login por email/password con formulario Thymeleaf
- CustomUserDetailsService para cargar usuarios desde BD

### Rutas Protegidas por Rol
| Rol | Acceso |
|-----|--------|
| ADMIN | `/admin/**`, `/productos/nuevo`, `/productos/{id}/editar`, `/productos/{id}/eliminar` |
| CLIENTE | `/carrito`, `/pedidos/**`, `/perfil` |
| Público | `/`, `/productos/**`, `/login`, `/registro`, `/css/**`, `/js/**`, `/img/**` |

### Lógica de Compra
- **Usuario anónimo**: puede ver productos, al comprar redirige a `/login`
- **Usuario autenticado**: puede agregar al carrito

---

## Endpoints Clave

#### Público
- `GET /` - Home con productos destacados
- `GET /productos` - Lista de productos disponibles
- `GET /productos/{id}` - Detalle del producto
- `GET /productos/buscar?q=...` - Búsqueda de productos

#### AUTH (público)
- `GET /login` - Formulario login
- `POST /login` - Procesar login
- `GET /logout` - Cerrar sesión
- `GET /registro` - Formulario registro
- `POST /registro` - Procesar registro

#### ADMIN (CRUD Productos - en /productos)
- `GET /productos/nuevo` - Formulario crear
- `POST /productos` - Crear producto
- `GET /productos/{id}/editar` - Formulario editar
- `POST /productos/{id}` - Actualizar producto
- `POST /productos/{id}/eliminar` - Eliminar producto

#### CLIENTE
- `GET /pedidos/mis-pedidos` - Ver mis pedidos (no cancelados)
- `GET /pedidos/{id}` - Ver detalle de pedido
- `POST /pedidos/{id}/cancelar` - Cancelar pedido (solo POR_CONFIRMAR)
- `GET /carrito` - Ver carrito
- `POST /carrito/agregar` - Agregar al carrito

#### ADMIN (Gestión de Pedidos)
- `GET /admin/pedidos` - Lista de pedidos
- `GET /admin/pedidos/{id}` - Detalle de pedido
- `POST /admin/pedidos/{id}/confirmar` - Confirmar y notificar por WhatsApp
- `POST /admin/pedidos/{id}/preparacion` - Iniciar preparación
- `POST /admin/pedidos/{id}/en-camino` - Enviar a domicilio
- `POST /admin/pedidos/{id}/entregado` - Confirmar entrega

#### ADMIN (Métodos de Pago)
- `GET /admin/metodo-pago` - Lista métodos de pago
- `GET /admin/metodo-pago/cuenta/nueva` - Formulario cuenta bancaria
- `POST /admin/metodo-pago/cuenta` - Guardar cuenta
- `GET /admin/metodo-pago/qr/nuevo` - Formulario QR
- `POST /admin/metodo-pago/qr` - Guardar QR

---

## Módulos Implementados

### Módulo Órdenes (CLIENTE)
- **DTO**: `OrderDTO` con `OrderItemDTO` anidado
- **Mapper**: `OrderMapper` (MapStruct)
- **Repository**: `OrderRepository`
- **Service**: `OrderService` (listar, crear, cancelar)
- **Controller**: `OrderController` (CLIENTE)

### Módulo Logging
- `LoggingControllerAdvice`: Registra usuario en sesión por request

### Módulo WhatsApp (Notificaciones wa.me)
- **Service**: `WhatsAppLinkService` genera enlaces wa.me
- **Admin**: Botón único "✓ Confirmar y Notificar" → abre WhatsApp con mensaje pre-llenado
- **Cliente**: Recibe enlace copiable para compartir por WhatsApp
- Configuración en `application.properties`:
  - `admin.whatsapp.number` - Número del administrador
  - `base.url` - URL base de la прилож