# AGENTS.md

## Stack
- **Spring Boot 3.5.14** (Java 21) — API REST + Thymeleaf + JPA
- **MySQL** (localhost:3306/appdomirest) — credenciales en `application.properties`
- **Lombok** — anotaciones procesadas via maven-compiler-plugin
- **MapStruct 1.6.3** — mapeo entre entidades y DTOs
- **Hibernate**: `ddl-auto=update` — crea/actualiza esquema automaticamente
- **Spring Security** — autenticación con BCrypt

## Comandos de desarrollo
```bash
./mvnw spring-boot:run          # ejecutar la app
./mvnw test                     # ejecutar pruebas
./mvnw test -Dtest=ClassName    # una clase de prueba
./mvnw package                  # compilar JAR
./mvnw compile -q               # compilar silencioso
```

## Arquitectura

### Modelo (`model/`)
- **`UserEntity`** — id, username, password, telefono, email, direcciones (ElementCollection), roles (ManyToMany EAGER), fechaCreacion, activo. Tabla: `users`
- **`RoleEntity`** — id, name (ERole: ADMIN, USER, DEV). Tabla: `roles`
- **`ProductoEntity`** — id, nombre, descripcion, stock, stockMinimo, precioCompra, precioVenta, imagenUrl, enPromocion, precioPromocional, codigo, activo, fechaCreacion, fechaActualizacion, usuarioCreador (ManyToOne). Tabla: `productos`
- **`VentaEntity`** — id, usuario (ManyToOne LAZY), fecha, estado (EVentaEstado), total, direccion, rutaDomicilio (ManyToOne), costoDomicilio, detalles (OneToMany cascade ALL orphanRemoval), esDomicilio, activo. Tabla: `ventas`
- **`DetalleVentaEntity`** — id, venta (ManyToOne LAZY), producto (ManyToOne LAZY), cantidad, precioUnitario, subtotal. Tabla: `detalle_ventas`
- **`RutaDomicilioEntity`** — id, barrio, precio, activo, fechaCreacion, fechaActualizacion. Tabla: `rutas_domicilio`
- **`UserDetailsImpl`** (model/userSecurity/) — implementa UserDetails para Spring Security
- **Enums**: `ERole` (ADMIN, USER, DEV), `EVentaEstado` (PENDIENTE, COMPLETADA, CANCELADA)

### DTOs (`dto/`)
```
dto/
├── user/
│   ├── UserRequest.java      (username, password, telefono, email, direccion, roles)
│   └── UserResponse.java     (sin password)
├── role/
│   └── RoleDTO.java          (id, name)
├── producto/
│   └── ProductoDTO.java      (todos los campos)
├── venta/
│   ├── VentaDTO.java         (incluye detalles, ruta, subtotal, costoDomicilio, getTotalGeneral)
│   └── DetalleVentaDTO.java  (incluye productoNombre, productoImagenUrl)
├── carrito/
│   ├── CarritoDTO.java       (items, total, cantidadItems)
│   ├── ItemCarritoDTO.java   (productoId, nombre, precio, imagenUrl, cantidad, subtotal)
│   ├── AgregarCarritoRequest.java
│   └── CheckoutRequest.java  (direccion, rutaId, esDomicilio)
└── ruta/
    └── RutaDomicilioDTO.java (id, barrio, precio, activo, fechas)
```

### Repositorios (`repository/`)
- `UserRepository` — findByUsername, findByTelefono, existsByUsername/Telefono/Email, countByActivo, findByRoles_Name, searchByUsernameOrTelefono
- `RoleRepository` — findByName, existsByName
- `ProductoRepository` — findByCodigo, findByActivo, findByActivo(Pageable), findAll(Pageable sobreescrito), findByNombreContainingIgnoreCase, countByActivo, findByStockLessThanEqual
- `VentaRepository` — findByUsuarioIdAndActivoTrue(Long), idem + Pageable, idem + FechaBetween(LocalDateTime, LocalDateTime, Pageable), findByActivoTrue(Pageable), findByEstadoAndActivoTrue
- `DetalleVentaRepository` — findByVentaId
- `RutaDomicilioRepository` — findByActivo, findByBarrioContainingIgnoreCase

### Services (`service/`)
- **`UserService`** — CRUD completo con DTOs, soft-delete, toggleActivo, findAllCustomers/searchCustomers (solo USER), findAllAdmins/saveAdmin/updateAdmin (ADMIN management)
- **`RoleService`** — CRUD basico, retorna RoleDTO
- **`ProductoService`** — CRUD completo, paginado, soft-delete, findStockBajo, findEnPromocion, searchByNombre (solo activos)
- **`VentaService`** — crearVenta (desde CarritoDTO, incluye costo domicilio), getVentasUsuario (paginado + filtro "hoy"/"todos"), getAllVentas (paginado), getVentasByEstado, getVentaById, completarVenta, cancelarVenta
- **`CarritoService`** — basado en HttpSession (clave "carrito"): getCarrito, agregarProducto, actualizarCantidad, removerProducto, limpiarCarrito
- **`CustomUserDetailsService`** — implementa UserDetailsService, busca por telefono o username
- **`ImageService`** — saveImage (valida tipo jpeg/png/gif/webp, max 10MB, guarda en static/imagenes/productos/), deleteImage
- **`RutaDomicilioService`** — CRUD completo, soft-delete, findAllActive

### Mappers (`mapper/`)
- `UserMapper`, `RoleMapper`, `ProductoMapper`, `VentaMapper`, `DetalleVentaMapper`, `RutaDomicilioMapper` (todos MapStruct interfaces)

### Security (`config/`)
- **`SecurityConfig.java`** — BCryptPasswordEncoder, SecurityFilterChain con rutas publicas (/, /catalogo, /producto/**, /buscar, /login, /register, /css/**, /js/**, /images/**, /imagenes/**), successHandler que redirige a /admin si ADMIN o /catalogo si USER
- **`RoleDataLoader.java`** — CommandLineRunner que inicializa roles ADMIN, USER, DEV

### Controllers (`controller/`)
- **`AuthController`** — GET /login, GET/POST /register, GET /home
- **`StoreController`** — GET / (landing con destacados), GET /catalogo (paginado), GET /producto/{id}, GET /buscar?q=
- **`CarritoController`** — GET /carrito, POST /carrito/agregar (JSON), POST /carrito/actualizar, POST /carrito/remover/{id}, POST /carrito/limpiar, POST /carrito/checkout (redirect a /pedido/{id}/confirmado), GET /carrito/count (JSON)
- **`VentasController`** — GET /mis-pedidos (paginado + filtro "todos"/"hoy"), GET /pedido/{id}/confirmado (verifica propiedad), GET /admin/ventas, GET /admin/ventas/{id}, POST /admin/ventas/{id}/completar, POST /admin/ventas/{id}/cancelar
- **`AdminController`** — GET /admin (dashboard con stats)
- **`ProductoController`** — CRUD admin de productos (GET/POST /admin/productos, GET /admin/productos/nuevo, GET/POST /admin/productos/{id}/editar, POST /admin/productos/{id}/eliminar)
- **`UserController`** — GET /admin/usuarios, POST /admin/usuarios/{id}/toggle, GET /admin/admins, GET/POST /admin/admins/nuevo, GET/POST /admin/admins/{id}/editar, POST /admin/admins/{id}/eliminar
- **`RutaDomicilioController`** — CRUD admin de rutas (GET/POST /admin/rutas, GET/POST /admin/rutas/nuevo, GET/POST /admin/rutas/{id}/editar, POST /admin/rutas/{id}/eliminar)

### Templates (`templates/`)
```
templates/
├── index.html                           # Landing page (hero, busqueda, destacados)
├── home.html                            # Post-login legacy
├── auth/
│   ├── login.html                       # Login editorial/luxury/dark
│   └── registro.html                    # Registro con validacion
├── store/
│   ├── catalogo.html                    # Grid productos paginado
│   ├── producto-detalle.html            # Detalle + agregar carrito
│   ├── buscar.html                      # Resultados busqueda
│   ├── carrito.html                     # Carrito + checkout (domicilio/recoger, rutas)
│   ├── mis-pedidos.html                 # Historial compacto (filtro hoy/todos, paginado)
│   └── pedido-confirmado.html           # Confirmacion post-checkout
├── admin/
│   ├── home-admin.html                  # Dashboard stats
│   ├── usuarios-list.html               # Lista clientes (busqueda, toggle activo)
│   ├── admins-list.html                 # Lista admins
│   ├── admin/admin-form.html            # Form admin
│   ├── productos-list.html              # Lista productos (tabla paginada)
│   ├── producto/productos-form.html     # Form producto con imagen
│   ├── ventas-list.html                 # Ventas admin (filtro estado)
│   ├── ventas-detail.html               # Detalle venta admin
│   ├── rutas-list.html                  # Lista rutas domicilio
│   └── ruta/ruta-form.html              # Form ruta
└── fragments/
    ├── store-head.html                  # <head> comun tienda (fonts, CSS, CSRF meta, titulo dinamico)
    ├── store-navbar.html                # Navbar tienda (Catálogo, Mis Pedidos, Carrito con badge, auth)
    ├── admin-head.html                  # <head> comun admin
    ├── admin-header.html                # Header admin con usuario
    ├── admin-sidebar.html               # Sidebar navegacion admin
    └── footer.html                      # Footer global
```

### Static resources (`static/`)
```
static/
├── css/
│   ├── global.css          # Base luxe, login (NO usar en admin)
│   ├── auth.css            # Estilos login/registro
│   ├── home.css            # Home legacy
│   ├── store.css           # Landing, catalogo, cards, grid, hero
│   ├── store-detail.css    # Detalle producto
│   ├── store-cart.css      # Carrito + checkout
│   ├── mis-pedidos.css     # Historial pedidos (cards, filtros, paginacion)
│   ├── admin.css           # Layout admin (sidebar, header, main)
│   └── admin-base.css      # Variables, tablas, forms, botones, badges admin
├── js/
│   ├── admin.js            # Funcionalidades admin
│   └── carrito.js          # Carrito: agregar, actualizar, badge contador, CSRF
└── imagenes/productos/     # Imagenes subidas de productos
```

## Frontend Design Conventions
- **Tipografía**: Playfair Display (titulos) + DM Sans (cuerpo)
- **Tema**: Oscuro/lujo con acentos dorados (#d4a853, #c9952b), glassmorphism, noise texture
- **Transiciones**: Staggered animations en cards, fade-in en hero, hover elevados
- **Variables CSS**: `--color-gold`, `--color-bg`, `--color-surface`, `--glass-bg`, `--glass-border`, `--font-display`, `--font-body`
- **Fragmentos**: store-head y store-navbar reutilizados en todas las paginas de tienda
- **Responsive**: Adaptable a movil con media queries

## Seguridad
- **PasswordEncoder**: BCryptPasswordEncoder
- **UserDetailsService**: CustomUserDetailsService carga usuario desde DB (busca por telefono o username)
- **Rutas publicas**: /, /catalogo, /producto/**, /buscar, /login, /register, /css/**, /js/**, /images/**, /imagenes/**
- **Rutas protegidas**: cualquier otra ruta requiere autenticación
- **Login success**: ADMIN redirige a /admin, USER redirige a /catalogo
- **Logout**: /logout redirige a /login?logout=true
- **Registro**: asigna rol USER por defecto; valida username, telefono, email, confirmPassword unicos

## Carrito & Pedidos
- **CarritoService** basado en `HttpSession`, clave `"carrito"`, tipo `CarritoDTO`
- **Checkout**: crea VentaEntity + DetalleVentaEntities, descuenta stock, asigna costo domicilio si esDomicilio, redirige a `/pedido/{id}/confirmado`
- **Mis Pedidos**: vista compacta (numero, fecha, estado, total, boton "Ver detalle"), paginada con filtro Hoy/Todos, ordenada por fecha DESC
- **Confirmacion**: `/pedido/{id}/confirmado` verifica que el pedido pertenezca al usuario autenticado
- **Soft-delete**: ventas y detalles tienen activo, no se eliminan fisicamente

## Gotchas
- `application.properties` contiene contrasenia MySQL hardcodeada; no hacer commit de credenciales
- `launch.json` de VS Code referencia `${workspaceFolder}/.env` para overrides locales
- Roles en UserEntity usan FetchType.EAGER
- Soft-delete: metodo delete() en servicios setea `activo=false`, no elimina registros
- MapStruct requiere Lombok annotation processor configurado en maven-compiler-plugin
- UserService usa UserRequest como entrada y UserResponse como salida (patrón DTO)
- VentaDTO tiene metodo `getTotalGeneral()` que suma subtotal + costoDomicilio
- El fragmento `store-navbar` debe mantenerse sincronizado si se agregan/quitan enlaces; todas las paginas de tienda lo usan via `th:replace`
- `carrito.js` espera un meta tag `<meta name="_csrf" content="...">` en el head (incluido en `store-head.html`)
- Paginacion: los templates de tienda y admin pasan `page` como query param
