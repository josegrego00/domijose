# Reglas para Generación de Código - Delivery App (Spring Boot)

---

## 1. Reglas Fundamentales de Arquitectura

- **Lenguaje y Versión**: Java 21, Spring Boot 4.0.5, Maven.
- **Punto de entrada principal**: `com.domicilio.domijose.DomijoseApplication`.
- **Estructura de Paquetes OBLIGATORIA**:

```
src/main/java/com/domicilio/domijose/
├── models/        # Entidades JPA
├── repositories/  # JPA Repositories
├── services/      # Lógica de negocio, con Logger
├── controllers/   # Thymeleaf Controllers
├── mappers/       # MapStruct Mappers
├── dto/           # usuario es el unico q tendra resposeDTO y requestDTO, el resto un único DTO por entidad el mismo debe servir para request como para response
└── config/        # Spring Security y configuración
```

- **Estilo API**: El proyecto es una aplicación web con vistas Thymeleaf. **NO** se deben generar controladores REST (`@RestController`) a menos que se indique explícitamente.

---

## 2. Reglas de Persistencia y Modelos

- **ORM**: Usar Spring Data JPA con MySQL.
- **Entidades Base**: Ubicadas en `models/`. Usar Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`).
- **Relaciones JPA**:

| Entidad | Relación | Entidad |
|---------|----------|---------|
| `User` | 1 — M | `Order` |
| `Order` | 1 — M | `OrderItem` |
| `Product` | 1 — M | `OrderItem` |

- **Enums Requeridos**:
  - `User.Role`: `CLIENTE` o `ADMIN`
  - `Order.Status`: `PENDIENTE` (carrito), `CONFIRMADO` (cliente selecciona método de pago), `EN_PREPARACION` (admin envía al restaurante), `EN_CAMINO` (domiciliario), `ENTREGADO` (cliente confirma), `CANCELADO` (solo si está antes de EN_CAMINO)
- **Validación**: **NO** usar anotaciones JSR-303 (`@NotNull`, `@Size`, etc.) en las entidades `models/`. Usar **solo en los DTOs** (`dto/`).

---

## 3. Reglas de Mapeo (DTOs y MapStruct)

- **DTOs**: (ej. `ProductDTO`). Este DTO sirve tanto para recibir datos (request) como para enviar (response).
- **Mappers**: Todos los mapeos entidad ↔ DTO deben hacerse usando **MapStruct**.
- **Estructura de Mapper**:

```java
@Mapper(componentModel = "spring")
public interface XyzMapper {
    XyzDTO toDto(Xyz entity);
    Xyz toEntity(XyzDTO dto);
}
```

---

## 4. Reglas de Seguridad (Spring Security)

- **Autenticación**: Login con numero de telefono y password (BCrypt), formulario Thymeleaf.
- **Carga de usuario**: Implementar `CustomUserDetailsService` que busca en `UserRepository` por numero de telefono.
- **Acceso por Rol** (redirigir a login si no autenticado):

| Rol | Acceso |
|-----|--------|
| **ADMIN** | `/admin/**`, `/productos/nuevo`, `/productos/{id}/editar`, `/productos/{id}/eliminar` |
| **CLIENTE** | `/carrito`, `/pedidos/**`, `/perfil` |
| **PÚBLICO** | `/`, `/productos/**`, `/login`, `/registro`, `/css/**`, `/js/**`, `/img/**` |

---

## 5. Reglas de Logging

- **Logger Obligatorio**: En todos los servicios (`src/main/java/.../services/`).
- **Formato estándar**:

```java
private static final Logger log = LoggerFactory.getLogger(NombreClase.class);
```

- **Niveles**:
  - `log.info()` → para acciones importantes (crear pedido, guardar producto, etc.)
  - `log.error()` → con la excepción capturada

---

## 6. Reglas de Negocio (Validaciones en Services)

- **Regla de return y atributos**: todos los metodos de los servicios deben recibir DTO y retornar DTO, la logica se debe hacer con las entidades.
- **Regla de Producto**: Antes de guardar/actualizar un producto en `ProductService`, validar que no exista otro producto con el mismo nombre (ignorando mayúsculas/minúsculas).
- **Regla de Pedido - Cancelación**: Solo se puede cancelar un pedido si su estado es `CONFIRMADO` (antes de EN_CAMINO).
- **Regla de Método de Pago**: El admin puede configurar cuentas bancarias y códigos QR en `/admin/metodo-pago`. El cliente ve opciones EFECTIVO, TRANSFERENCIA, DATAFONO en checkout.

---

## 7. Reglas de UI/UX (Thymeleaf y CSS)

- **Enfoque**: Mobile-first.
- **Ubicación de recursos estáticos**:

```
src/main/resources/
├── static/
│   ├── css/          ← Estilos personalizados
│   ├── js/           ← Scripts JavaScript
│   └── images/
│       └── productos/ ← Imágenes de productos
└── templates/        ← Vistas Thymeleaf
```

- **Uso de fragmentos**: Usar `fragmentos/header.html` y `fragmentos/footer.html` para evitar duplicación.
- **Estilo CSS mínimo**: No usar frameworks externos. Usar clases personalizadas.
- **Paleta de colores fija**:
## 7. Reglas de UI/UX (Thymeleaf y CSS)

- **Enfoque**: Mobile-first.
- **Viewport**: `maximum-scale=1.0, user-scalable=no` (evita zoom automático)
- **Ancho**: 100%, max-width 500px (formularios), grid responsivo
- **Font-size**: 16px mínimo (evita zoom en iOS)
- **Padding touch**: 14px-16px para botones e inputs
- **Border-radius**: 12px (inputs), 50px (botones píldora)

### Paleta de Colores (Estilo Restaurante)

| Uso | Color | Hexadecimal |
|-----|-------|-------------|
| Rojo principal (botones primarios) | Rojo tomate | `#FF4D4D` |
| Naranja principal (hovers, acentos) | Naranja vibrante | `#FF8C42` |
| Amarillo (destacados, ofertas) | Amarillo cálido | `#FFD166` |
| Fondo degradado | Cremoso suave | `#FFF9F0` → `#FFFFFF` |
| Texto oscuro | Marrón suave | `#4A2A1A` |
| Texto claro | Blanco hueso | `#FFFDF9` |
| Bordes y sombras | Marrón claro | `#E8D5B5` |

### Estilos CSS Base

```css
/* Variables de color */
:root {
    --rojo-principal: #FF4D4D;
    --rojo-hover: #E64444;
    --naranja: #FF8C42;
    --naranja-hover: #E67A33;
    --amarillo: #FFD166;
    --fondo: linear-gradient(135deg, #FFF9F0 0%, #FFFFFF 100%);
    --texto-oscuro: #4A2A1A;
    --texto-claro: #FFFDF9;
    --borde: #E8D5B5;
    --sombra: 0 4px 12px rgba(0,0,0,0.08);
}

/* Mobile-first */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;
    background: var(--fondo);
    color: var(--texto-oscuro);
    min-height: 100vh;
}

/* Botones */
.btn-primary {
    background: var(--rojo-principal);
    color: var(--texto-claro);
    border: none;
    padding: 14px 24px;
    border-radius: 50px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    width: 100%;
    max-width: 300px;
}

.btn-primary:hover {
    background: var(--rojo-hover);
    transform: scale(1.02);
}

.btn-secondary {
    background: var(--naranja);
    color: var(--texto-claro);
    border: none;
    padding: 12px 20px;
    border-radius: 50px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
}

.btn-secondary:hover {
    background: var(--naranja-hover);
}

.btn-outline {
    background: transparent;
    border: 2px solid var(--rojo-principal);
    color: var(--rojo-principal);
    padding: 12px 20px;
    border-radius: 50px;
    font-weight: 600;
    cursor: pointer;
}

/* Tarjetas de productos */
.card {
    background: white;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: var(--sombra);
    transition: transform 0.3s ease;
    margin-bottom: 16px;
}

.card:hover {
    transform: translateY(-4px);
}

.card-img {
    width: 100%;
    height: 200px;
    object-fit: cover;
}

.card-content {
    padding: 16px;
}

.card-title {
    font-size: 18px;
    font-weight: 700;
    color: var(--texto-oscuro);
    margin-bottom: 8px;
}

.card-price {
    font-size: 20px;
    font-weight: 700;
    color: var(--rojo-principal);
}

.badge-oferta {
    background: var(--amarillo);
    color: var(--texto-oscuro);
    padding: 4px 12px;
    border-radius: 50px;
    font-size: 12px;
    font-weight: 600;
    display: inline-block;
}

/* Inputs */
input, textarea, select {
    width: 100%;
    padding: 14px 16px;
    border: 1px solid var(--borde);
    border-radius: 12px;
    font-size: 16px;
    background: white;
    transition: border 0.3s ease;
}

input:focus, textarea:focus, select:focus {
    outline: none;
    border-color: var(--naranja);
    box-shadow: 0 0 0 3px rgba(255, 140, 66, 0.1);
}

/* Grid responsivo */
.container {
    padding: 16px;
    max-width: 1200px;
    margin: 0 auto;
}

.grid {
    display: grid;
    grid-template-columns: 1fr;
    gap: 16px;
}

@media (min-width: 640px) {
    .grid {
        grid-template-columns: repeat(2, 1fr);
        gap: 20px;
    }
    
    .container {
        padding: 24px;
    }
}

@media (min-width: 1024px) {
    .grid {
        grid-template-columns: repeat(3, 1fr);
        gap: 24px;
    }
}

/* Header / Navbar */
.navbar {
    background: white;
    padding: 12px 16px;
    box-shadow: var(--sombra);
    position: sticky;
    top: 0;
    z-index: 100;
}

.navbar-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    max-width: 1200px;
    margin: 0 auto;
}

.logo {
    font-size: 24px;
    font-weight: 800;
    background: linear-gradient(135deg, var(--rojo-principal), var(--naranja));
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    text-decoration: none;
}

.logo span {
    color: var(--naranja);
}

.nav-links {
    display: flex;
    gap: 20px;
    align-items: center;
}

.nav-links a {
    color: var(--texto-oscuro);
    text-decoration: none;
    font-weight: 500;
}

.nav-links a:hover {
    color: var(--naranja);
}

/* Carrito badge */
.cart-badge {
    position: relative;
}

.cart-count {
    position: absolute;
    top: -8px;
    right: -12px;
    background: var(--rojo-principal);
    color: white;
    font-size: 12px;
    padding: 2px 6px;
    border-radius: 50px;
}

/* Alertas */
.alert-success {
    background: #D4EDDA;
    color: #155724;
    padding: 12px 16px;
    border-radius: 12px;
    margin-bottom: 16px;
    border-left: 4px solid #28A745;
}

.alert-error {
    background: #F8D7DA;
    color: #721C24;
    padding: 12px 16px;
    border-radius: 12px;
    margin-bottom: 16px;
    border-left: 4px solid var(--rojo-principal);
}

/* Footer */
.footer {
    background: var(--texto-oscuro);
    color: var(--texto-claro);
    text-align: center;
    padding: 24px 16px;
    margin-top: 48px;
}

---

## 8. Flujo de Compra (Carrito/Cesta)

- **Lógica**: El carrito se maneja en memoria (sesión) o con cookies. **No** requiere entidad propia en la base de datos.
- **Usuario anónimo**: Puede ver productos y agregar al carrito, pero al precionar comprar, redirigir a `/login`.
- **Usuario autenticado**: Puede agregar al carrito y proceder al checkout (crear `Order`).

---

## 9. Reglas de Controladores

- **Tipo**: `@Controller` (para Thymeleaf), **no** `@RestController`.
- **Redirecciones**:
  - POST exitoso (crear/actualizar) → `redirect:/ruta` (ej. `redirect:/productos`)
  - POST con error → retorna al mismo formulario con el modelo
- **Manejo de sesión**: Usar `@ModelAttribute` o `HttpSession` para el carrito.
  **Manejo de DTO** deben manejar los metodos DTOs

---

## 10. Comandos de Ejecución

| Acción | Comando |
|--------|---------|
| Ejecutar app | `./mvnw spring-boot:run` |
| Compilar | `./mvnw package` |
| Tests | `./mvnw test` |

> **Nota**: La base de datos MySQL debe estar ejecutándose localmente antes de iniciar la app.

---

*Estas reglas deben seguirse estrictamente para mantener la consistencia y calidad del código en el proyecto Domijose.*
