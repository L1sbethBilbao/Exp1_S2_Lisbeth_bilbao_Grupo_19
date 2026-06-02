# Informe Semana 2 — Autenticación y Autorización con Spring Security y JWT

**Proyecto:** MiniMarket Plus  
**Asignatura:** Desarrollo Backend II

---

## 1. Análisis y selección de estrategia de seguridad

### Contexto
MiniMarket Plus requiere proteger su API REST sin mantener sesiones en el servidor, alineado con arquitecturas stateless y futuros microservicios.

### Estrategia seleccionada
| Decisión | Justificación |
|----------|---------------|
| **Spring Security** | Estándar en ecosistema Spring; integración con filtros, roles y `UserDetailsService`. |
| **JWT (jjwt 0.12.6)** | Tokens firmados que transportan identidad y roles sin estado en servidor. |
| **BCrypt** | Hash irreversible para contraseñas; resistente a fuerza bruta. |
| **Autorización por roles** | Modelo de negocio claro: CLIENTE, EMPLEADO, GERENTE. |
| **`@PreAuthorize`** | Reglas explícitas por endpoint, auditables en código. |

### Alternativas descartadas
- **Sesiones HTTP:** no escalan bien en múltiples instancias sin sticky sessions o Redis.
- **OAuth2 completo:** excesivo para esta fase; JWT propio cubre el caso formativo.

---

## 2. Configuración paso a paso

### Paso 1 — Dependencias (`pom.xml`)
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (0.12.6)

### Paso 2 — Autenticación JWT

1. **`ConfigSpringSecurity`** (alineado con repo del profesor): deshabilita form login y HTTP Basic; política `STATELESS`; filtro JWT antes de `UsernamePasswordAuthenticationFilter`; `DaoAuthenticationProvider` con BCrypt.
2. **`JwtProperties`**: mapea `jwt.secret` y `jwt.expiration` desde `application.properties`.
3. **Entidad `Usuario`**: username único, password con BCrypt, relación `ManyToMany` con `Rol`.
4. **`CustomUserDetailsService`**: implementa `UserDetailsService` cargando usuario desde H2.
5. **`AuthController`** (en `security/controller/`): `POST /api/auth/register` y `POST /api/auth/login`.
6. **`JwtUtil`**: genera token con `subject`, `roles` en claims, firma HMAC-SHA256 y expiración configurable vía `JwtProperties`.
7. **`JwtAuthenticationFilter`**: lee `Authorization: Bearer <token>`, valida y establece `SecurityContext` en cada request.

### Paso 3 — Autorización por roles

| Recurso | CLIENTE | EMPLEADO | GERENTE |
|---------|---------|----------|---------|
| Catálogo (GET productos/categorías) | Sí | Sí | Sí |
| Carrito / ventas (lectura y compra) | Sí | Sí | Sí |
| Inventario | No | Sí | Sí |
| CRUD productos/categorías (POST/PUT) | No | Sí | Sí |
| DELETE en cualquier módulo | No | No | Sí |
| Gestión de usuarios | No | No | Sí |

Roles inicializados en `DataInitializer` con usuarios de prueba.

---

## 3. Protección frente a amenazas

| Amenaza | Medida implementada |
|---------|---------------------|
| Credenciales en texto plano | BCrypt en registro, login y alta por gerente |
| Suplantación de identidad | JWT firmado con clave secreta ≥ 256 bits (HS256) |
| Token manipulado | Verificación de firma en `JwtUtil.validateToken` |
| Acceso no autorizado | `@PreAuthorize` + respuestas 401/403 JSON |
| Exposición de contraseñas en API | `@JsonProperty(WRITE_ONLY)` en entidad `Usuario` |
| CSRF en API stateless | CSRF deshabilitado (patrón REST + Bearer token) |

---

## 4. Respuestas a preguntas de apoyo

**1. ¿Cómo funciona JWT stateless?**  
El cliente envía el token en cada petición. El servidor valida firma y expiración sin consultar sesión almacenada.

**2. ¿Ventajas sobre sesiones?**  
Escalabilidad horizontal, menor acoplamiento entre servicios y sin memoria de sesión centralizada.

**3. ¿Cómo evitar manipulación?**  
Firma HMAC con clave secreta robusta; no confiar en claims sin verificar; expiración corta (24 h configurable).

**4. ¿Autorización por roles?**  
Authorities `ROLE_CLIENTE`, `ROLE_EMPLEADO`, `ROLE_GERENTE` en `CustomUserDetails` y restricciones con `@PreAuthorize`.

**5. ¿Buenas prácticas?**  
Stateless, BCrypt, validación de entrada, handlers 401/403, secret externalizable, endpoints públicos mínimos (`/api/auth/**`, `/public/**`).

---

## 5. Guía de pruebas con Postman o curl

### Registro
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{"username":"nuevo_cliente","password":"clave123"}
```

### Login
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{"username":"gerente1","password":"gerente123"}
```

### Acceso protegido
```http
GET http://localhost:8080/api/productos
Authorization: Bearer <token_del_login>
```

### Usuarios de demostración
| Usuario | Contraseña | Rol |
|---------|------------|-----|
| cliente1 | cliente123 | CLIENTE |
| empleado1 | empleado123 | EMPLEADO |
| gerente1 | gerente123 | GERENTE |

---

## 6. Archivos principales modificados o creados

- `security/config/ConfigSpringSecurity.java`
- `security/config/JwtProperties.java`
- `security/util/JwtUtil.java`
- `security/filter/JwtAuthenticationFilter.java`
- `security/service/AuthService.java`
- `security/controller/AuthController.java`
- `config/DataInitializer.java`
- Controladores con `@PreAuthorize`
- `pom.xml`, `application.properties`
