# Checklist — Actividad Semana 2 MiniMarket Plus

**Uso:** revisa este archivo antes de entregar al aula virtual.  
**Objetivo:** máximo puntaje (Completamente Logrado — 100%) según la pauta.

---

## Veredicto general

| Ámbito | ¿Cumple para máximo puntaje? |
|--------|------------------------------|
| **Código del backend (Pasos 1–3)** | **Sí** |
| **Pruebas de acceso por rol** | **Sí** (21/21 verificadas) |
| **Informe (Paso 4 / criterio 7)** | **Sí** (reforzar con capturas Postman) |
| **Entrega en el aula** | Depende de que **adjuntes** proyecto + informe + evidencia |

---

## Pauta de evaluación → 7 criterios CL (100%)

### Criterio 1 — Dependencias Spring Security y JWT

**Estado: CUMPLE**

- `spring-boot-starter-security` en `minimarket/pom.xml`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (versión 0.12.6)
- `spring-boot-starter-validation`
- El proyecto compila y arranca

**Archivos clave:** `minimarket/pom.xml`

---

### Criterio 2 — Seguridad personalizada + JWT (sin auth por defecto)

**Estado: CUMPLE**

- `SecurityConfig`: sin form login ni HTTP Basic
- Sesión `STATELESS` (sin estado en servidor)
- `JwtAuthenticationFilter` **antes** de `UsernamePasswordAuthenticationFilter`
- Rutas públicas: `/api/auth/**`, `/public/**`
- Resto de rutas: requieren autenticación con Bearer token

**Archivos clave:** `security/config/SecurityConfig.java`, `security/filter/JwtAuthenticationFilter.java`

**Nota:** En Spring Boot 3 no se usa `WebSecurityConfigurerAdapter`; se usa `SecurityFilterChain` con `@Bean`. Es la forma actual y correcta. Si el profesor menciona “extender” la clase antigua, explícalo en el informe en una línea.

---

### Criterio 3 — Entidad Usuario con hashing de contraseñas

**Estado: CUMPLE**

- Entidad `Usuario`: `id`, `username`, `password`, `roles` (relación ManyToMany con `Rol`)
- BCrypt en:
  - Registro (`AuthService`)
  - Usuarios demo al iniciar (`DataInitializer`)
  - Alta/actualización por gerente (`UsuarioServiceImpl`)
- La contraseña no se devuelve en JSON (`@JsonProperty(WRITE_ONLY)`)

**Archivos clave:** `entity/Usuario.java`, `entity/Rol.java`, `config/DataInitializer.java`

---

### Criterio 4 — JwtUtil con claims y firma segura

**Estado: CUMPLE**

- Clase `JwtUtil` con HMAC-SHA256
- Claims: `subject` (username), `roles`
- Expiración configurable en `application.properties` (`jwt.secret`, `jwt.expiration-ms`)
- Métodos de generación y validación de tokens

**Archivos clave:** `security/util/JwtUtil.java`, `src/main/resources/application.properties`

---

### Criterio 5 — Filtro JWT antes de la autenticación estándar

**Estado: CUMPLE**

- `JwtAuthenticationFilter` extiende `OncePerRequestFilter`
- Lee encabezado `Authorization: Bearer <token>`
- Valida token y establece `SecurityContext`

**Archivos clave:** `security/filter/JwtAuthenticationFilter.java`

---

### Criterio 6 — Roles y permisos en endpoints

**Estado: CUMPLE**

**Roles definidos:** CLIENTE, EMPLEADO, GERENTE

| Recurso | CLIENTE | EMPLEADO | GERENTE |
|---------|---------|----------|---------|
| GET productos / categorías | Sí | Sí | Sí |
| Carrito, ventas, detalle venta | Sí | Sí | Sí |
| Inventario | No | Sí | Sí |
| POST/PUT productos y categorías | No | Sí | Sí |
| DELETE (productos, categorías, etc.) | No | No | Sí |
| Gestión de usuarios (`/api/usuarios`) | No | No | Sí |

- `@PreAuthorize` en todos los controladores de negocio
- Constantes en `SecurityExpressions` y `SecurityRoles`

**Pruebas verificadas:** 21/21 OK

- Script: `minimarket/test-secuencial.ps1`
- Postman: `minimarket/postman/MiniMarket_Semana2.postman_collection.json`
- Guía: `minimarket/postman/GUIA_PRUEBAS_SECUENCIALES.md`

**Usuarios demo:**

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| cliente1 | cliente123 | CLIENTE |
| empleado1 | empleado123 | EMPLEADO |
| gerente1 | gerente123 | GERENTE |

---

### Criterio 7 — Informe completo

**Estado: CUMPLE** (mejorar con capturas al entregar)

**Archivo:** `INFORME_SEMANA_2.md` (carpeta `2_semana`)

Debe incluir (y el informe ya cubre):

1. Análisis y justificación de estrategia (Paso 1 entregable)
2. Configuración paso a paso (Paso 2 entregable)
3. Autorización por roles (Paso 3 entregable)
4. Protección frente a amenazas
5. Respuestas a las 5 preguntas de apoyo de la actividad

**Antes de entregar, añade al informe o como anexo:**

- [ ] Capturas del Collection Runner de Postman (requests en verde)
- [ ] Mención de la colección Postman importada
- [ ] (Opcional) Diagrama simple del flujo: login → JWT → request protegido

---

## Actividad oficial — Pasos 1 a 4

| Paso | Requerido | ¿Hecho? |
|------|-----------|---------|
| **Paso 1** | `spring-boot-starter-security` + jjwt en Maven | Sí |
| **Paso 2a** | Security personalizada + JWT | Sí |
| **Paso 2b** | Entidad Usuario + BCrypt | Sí |
| **Paso 2c** | `UserDetailsService` | Sí (`CustomUserDetailsService`) |
| **Paso 2d** | Registro + login | Sí (`/api/auth/register`, `/api/auth/login`) |
| **Paso 2e** | `JwtUtil` | Sí |
| **Paso 2f** | Filtro JWT | Sí |
| **Paso 3** | Roles + `@PreAuthorize` + pruebas | Sí |
| **Paso 4** | Informe detallado | Sí (`INFORME_SEMANA_2.md`) |

### Preguntas de apoyo (deben estar en el informe)

1. ¿Cómo funciona JWT en aplicación stateless?
2. ¿Ventajas de JWT vs sesiones tradicionales?
3. ¿Cómo evitar manipulación del token?
4. ¿Cómo implementaste autorización por roles?
5. ¿Qué buenas prácticas aplicaste?

---

## Qué adjuntar en el aula virtual

- [ ] Carpeta/proyecto **`minimarket`** (código fuente)
- [ ] **`INFORME_SEMANA_2.md`** (o exportado a PDF/Word)
- [ ] **`MiniMarket_Semana2.postman_collection.json`** (opcional pero recomendado)
- [ ] Capturas o PDF con resultado de pruebas (21/21)
- [ ] Este checklist (opcional, para ti)

---

## Cómo ejecutar y demostrar

### 1. Iniciar la aplicación

```powershell
cd minimarket
.\mvnw.cmd spring-boot:run
```

Esperar: `Started MinimarketApplication` en consola.

### 2. Pruebas automáticas (PowerShell)

```powershell
cd minimarket
powershell -ExecutionPolicy Bypass -File test-secuencial.ps1
```

Resultado esperado: `Pasaron: 21 / 21 | Fallaron: 0`

### 3. Pruebas en Postman

1. Import → `minimarket/postman/MiniMarket_Semana2.postman_collection.json`
2. Variable `baseUrl` = `http://localhost:8080`
3. Collection Runner → ejecutar **en orden**, sin paralelizar

Ver detalle en: `minimarket/postman/GUIA_PRUEBAS_SECUENCIALES.md`

---

## Lista secuencial de endpoints (21 pruebas)

| # | Request | Método | URL | Auth | Esperado |
|---|---------|--------|-----|------|----------|
| 01 | Público | GET | `/public/hola` | No | 200 |
| 02 | Sin token | GET | `/api/productos` | No | 401 |
| 03 | Registro | POST | `/api/auth/register` | No | 201 o 409 |
| 04 | Login CLIENTE | POST | `/api/auth/login` | No | 200 |
| 05 | Listar productos | GET | `/api/productos` | Bearer CLIENTE | 200 |
| 06 | Crear producto | POST | `/api/productos` | Bearer CLIENTE | 403 |
| 07 | Inventario | GET | `/api/inventario` | Bearer CLIENTE | 403 |
| 08 | Login EMPLEADO | POST | `/api/auth/login` | No | 200 |
| 09 | Crear categoría | POST | `/api/categorias` | Bearer EMPLEADO | 200 |
| 10 | Crear producto | POST | `/api/productos` | Bearer EMPLEADO | 200 |
| 11 | Registrar inventario | POST | `/api/inventario` | Bearer EMPLEADO | 200 |
| 12 | Listar inventario | GET | `/api/inventario` | Bearer EMPLEADO | 200 |
| 13 | Eliminar producto | DELETE | `/api/productos/{id}` | Bearer EMPLEADO | 403 |
| 14 | Login GERENTE | POST | `/api/auth/login` | No | 200 |
| 15 | Listar usuarios | GET | `/api/usuarios` | Bearer GERENTE | 200 |
| 16 | Listar usuarios | GET | `/api/usuarios` | Bearer CLIENTE | 403 |
| 17 | Agregar carrito | POST | `/api/carrito` | Bearer CLIENTE | 200 |
| 18 | Crear venta | POST | `/api/ventas` | Bearer CLIENTE | 200 |
| 19 | Detalle venta | POST | `/api/detalle-ventas` | Bearer CLIENTE | 200 |
| 20a | Producto para borrar | POST | `/api/productos` | Bearer EMPLEADO | 200 |
| 20b | Eliminar producto | DELETE | `/api/productos/{id}` | Bearer GERENTE | 204 |
| 21 | Login inválido | POST | `/api/auth/login` | No | 401 |

**Nota paso 20:** no se elimina el producto usado en ventas/carrito/inventario (integridad referencial). Se crea un producto `TempDelete` solo para demostrar DELETE del gerente.

---

## Archivos importantes del proyecto

```
2_semana/
├── INFORME_SEMANA_2.md              ← Informe para entregar
├── CHECKLIST_ENTREGA_SEMANA_2.md    ← Este archivo
├── actividad_semana_2.txt
├── pauta_de_evaluacion_semana_2.txt
└── minimarket/
    ├── pom.xml
    ├── test-secuencial.ps1
    ├── postman/
    │   ├── MiniMarket_Semana2.postman_collection.json
    │   └── GUIA_PRUEBAS_SECUENCIALES.md
    └── src/main/java/com/minimarket/
        ├── controller/AuthController.java
        ├── config/DataInitializer.java
        ├── config/SecurityConfig.java (en security/config/)
        ├── security/
        │   ├── filter/JwtAuthenticationFilter.java
        │   ├── util/JwtUtil.java
        │   ├── service/AuthService.java
        │   └── service/CustomUserDetailsService.java
        └── entity/Usuario.java, Rol.java
```

---

## Buenas prácticas implementadas (para mencionar en defensa oral o informe)

- API stateless con JWT
- Contraseñas con BCrypt (nunca en texto plano)
- Validación de entrada en registro/login
- Respuestas 401 y 403 en JSON (`JwtAuthenticationEntryPoint`, `JwtAccessDeniedHandler`)
- Clave JWT configurable (en producción: variable de entorno)
- `@PreAuthorize` explícito por endpoint
- Contraseña oculta en respuestas JSON
- Manejo de conflicto al eliminar registros con FK (409)

---

## Riesgos que NO te bajan puntaje si explicas

| Tema | Situación |
|------|-----------|
| Microservicios | La actividad lo menciona como **contexto**; no exige varios servicios desplegados |
| `WebSecurityConfigurerAdapter` | Obsoleto en Spring Boot 3; tu proyecto usa `SecurityFilterChain` (correcto) |
| Categoría duplicada en Postman | Si falla el paso 09, reinicia la app (H2 en memoria) o cambia el nombre de la categoría |

---

## Conclusión

**El proyecto cumple la actividad y los 7 criterios de la pauta al nivel “Completamente Logrado (100%)”,** si entregas:

1. Código (`minimarket`)
2. Informe (`INFORME_SEMANA_2.md`)
3. Evidencia de pruebas (Postman o script 21/21)

**Sin informe o sin pruebas documentadas** podrías perder puntos en el criterio 7 y en el Paso 3 de la actividad.

---

*Generado para la Semana 2 — Desarrollo Backend II — MiniMarket Plus*
