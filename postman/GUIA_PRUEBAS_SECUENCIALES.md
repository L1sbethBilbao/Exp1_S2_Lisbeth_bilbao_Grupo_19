# Guía de pruebas secuenciales — Semana 2 MiniMarket

**Estado:** las 21 pruebas automatizadas pasaron correctamente (`test-secuencial.ps1`).

## Requisitos previos

1. Iniciar la aplicación:
   ```powershell
   cd minimarket
   .\mvnw.cmd spring-boot:run
   ```
2. Esperar el mensaje `Started MinimarketApplication` en consola.

## Usuarios de demostración

| Usuario    | Contraseña   | Rol      |
|-----------|--------------|----------|
| cliente1  | cliente123   | CLIENTE  |
| empleado1 | empleado123  | EMPLEADO |
| gerente1  | gerente123   | GERENTE  |

---

## Lista secuencial (22 requests en Postman / 21 pasos lógicos)

| # | Request | Método | URL | Auth | Resultado esperado |
|---|---------|--------|-----|------|-------------------|
| 01 | Público | GET | `/public/hola` | No | **200** |
| 02 | Sin autenticación | GET | `/api/productos` | No | **401** |
| 03 | Registro | POST | `/api/auth/register` | No | **201** (o 409 si ya existe) |
| 04 | Login CLIENTE | POST | `/api/auth/login` | No | **200** + guardar `token` |
| 05 | Listar productos | GET | `/api/productos` | Bearer CLIENTE | **200** |
| 06 | Crear producto | POST | `/api/productos` | Bearer CLIENTE | **403** |
| 07 | Ver inventario | GET | `/api/inventario` | Bearer CLIENTE | **403** |
| 08 | Login EMPLEADO | POST | `/api/auth/login` | No | **200** + guardar `token` |
| 09 | Crear categoría | POST | `/api/categorias` | Bearer EMPLEADO | **200** → guardar `categoriaId` |
| 10 | Crear producto | POST | `/api/productos` | Bearer EMPLEADO | **200** → guardar `productoId` |
| 11 | Registrar inventario | POST | `/api/inventario` | Bearer EMPLEADO | **200** |
| 12 | Listar inventario | GET | `/api/inventario` | Bearer EMPLEADO | **200** |
| 13 | Eliminar producto | DELETE | `/api/productos/{productoId}` | Bearer EMPLEADO | **403** |
| 14 | Login GERENTE | POST | `/api/auth/login` | No | **200** + guardar `token` |
| 15 | Listar usuarios | GET | `/api/usuarios` | Bearer GERENTE | **200** → guardar `clienteId` de cliente1 |
| 16 | Listar usuarios | GET | `/api/usuarios` | Bearer CLIENTE | **403** |
| 17 | Agregar al carrito | POST | `/api/carrito` | Bearer CLIENTE | **200** |
| 18 | Crear venta | POST | `/api/ventas` | Bearer CLIENTE | **200** → guardar `ventaId` |
| 19 | Detalle de venta | POST | `/api/detalle-ventas` | Bearer CLIENTE | **200** |
| 20a | Crear producto temporal | POST | `/api/productos` | Bearer EMPLEADO | **200** → `productoDeleteId` |
| 20b | Eliminar producto | DELETE | `/api/productos/{productoDeleteId}` | Bearer GERENTE | **204** |
| 21 | Login inválido | POST | `/api/auth/login` | No | **401** |

> **Nota paso 20:** no se elimina el `productoId` del flujo de ventas porque tiene registros en inventario, carrito y detalle de venta (integridad referencial). Se crea un producto aparte solo para demostrar el DELETE del GERENTE.

---

## Importar en Postman

1. Abrir Postman → **Import** → seleccionar:
   `minimarket/postman/MiniMarket_Semana2.postman_collection.json`
2. Ejecutar con **Collection Runner** en orden (sin paralelizar).
3. Variable `baseUrl` = `http://localhost:8080` (ya configurada).

Los scripts de prueba guardan automáticamente los tokens y los IDs necesarios entre requests.

---

## Ejecutar pruebas por script (PowerShell)

```powershell
cd minimarket
.\mvnw.cmd spring-boot:run
# En otra terminal:
powershell -ExecutionPolicy Bypass -File test-secuencial.ps1
```

Debe mostrar: `Pasaron: 21 / 21 | Fallaron: 0`

---

## Qué demuestra cada bloque (pauta de evaluación)

| Bloque | Criterio de la pauta |
|--------|----------------------|
| 01–03 | Endpoints públicos y registro |
| 04–07 | JWT + rol CLIENTE |
| 08–13 | Rol EMPLEADO y denegación de DELETE |
| 14–16 | Rol GERENTE y denegación a CLIENTE |
| 17–19 | Operaciones de compra (CLIENTE autenticado) |
| 20 | Solo GERENTE puede eliminar |
| 21 | Manejo de credenciales inválidas |
