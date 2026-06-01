$base = "http://localhost:8080"
$results = @()
$suffix = Get-Random -Maximum 99999

function Get-Token([string]$u, [string]$p) {
    $b = @{ username = $u; password = $p } | ConvertTo-Json
    $t = (Invoke-RestMethod -Uri "$base/api/auth/login" -Method POST -ContentType "application/json" -Body $b).token
    if (-not $t) { throw "Token vacio para $u" }
    return $t
}

function AuthHeader([string]$token) {
    return @{ Authorization = "Bearer $token" }
}

function Log([int]$n, [string]$name, [int]$status, [string]$exp, [string]$extra = "") {
    $ok = $status -in ($exp -split ',')
    $r = if ($ok) { "OK" } else { "FAIL" }
    $script:results += [PSCustomObject]@{ Num = $n; Name = $name; Status = $status; Expected = $exp; Result = $r }
    Write-Host "[$r] #$n $name -> $status (esperado: $exp) $extra"
}

function Try-Status([scriptblock]$fn) {
    try { & $fn | Out-Null; return 200 }
    catch { if ($_.Exception.Response) { return [int]$_.Exception.Response.StatusCode.value__ }; throw }
}

Write-Host "=== Prueba secuencial MiniMarket Semana 2 ===" -ForegroundColor Cyan

$s = (Invoke-WebRequest -Uri "$base/public/hola" -UseBasicParsing).StatusCode
Log 1 "Public - Hola" $s "200"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/productos" -ErrorAction Stop }
Log 2 "Sin auth - Productos" $s "401"

$reg = @{ username = "user_$suffix"; password = "test1234" } | ConvertTo-Json
try { Invoke-RestMethod -Uri "$base/api/auth/register" -Method POST -ContentType "application/json" -Body $reg | Out-Null; $s = 201 }
catch { $s = [int]$_.Exception.Response.StatusCode.value__ }
Log 3 "Registro nuevo cliente" $s "201,409"

$tC = Get-Token "cliente1" "cliente123"
Log 4 "Login CLIENTE" 200 "200"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/productos" -Headers (AuthHeader $tC) -ErrorAction Stop }
Log 5 "CLIENTE - Listar productos" $s "200"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/productos" -Method POST -Headers (AuthHeader $tC) -ContentType "application/json" -Body '{"nombre":"X","precio":1,"stock":1,"categoria":{"id":1}}' -ErrorAction Stop }
Log 6 "CLIENTE - Crear producto (403)" $s "403"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/inventario" -Headers (AuthHeader $tC) -ErrorAction Stop }
Log 7 "CLIENTE - Inventario (403)" $s "403"

$tE = Get-Token "empleado1" "empleado123"
Log 8 "Login EMPLEADO" 200 "200"

$catName = "Cat_$suffix"
$cat = Invoke-RestMethod -Uri "$base/api/categorias" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body "{`"nombre`":`"$catName`"}"
$catId = $cat.id
Log 9 "EMPLEADO - Crear categoria" 200 "200" "catId=$catId"

$prod = Invoke-RestMethod -Uri "$base/api/productos" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body "{`"nombre`":`"Agua`",`"precio`":500,`"stock`":50,`"categoria`":{`"id`":$catId}}"
$prodId = $prod.id
Log 10 "EMPLEADO - Crear producto" 200 "200" "prodId=$prodId"

$tE = Get-Token "empleado1" "empleado123"
$invBody = "{`"producto`":{`"id`":$prodId},`"cantidad`":10,`"tipoMovimiento`":`"Entrada`",`"fechaMovimiento`":`"2026-06-01T12:00:00.000Z`"}"
$s = Try-Status { Invoke-RestMethod -Uri "$base/api/inventario" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body $invBody -ErrorAction Stop }
Log 11 "EMPLEADO - Registrar inventario" $s "200"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/inventario" -Headers (AuthHeader $tE) -ErrorAction Stop }
Log 12 "EMPLEADO - Listar inventario" $s "200"

try { Invoke-RestMethod -Uri "$base/api/productos/$prodId" -Method Delete -Headers (AuthHeader $tE) -ErrorAction Stop | Out-Null; $s = 204 }
catch { $s = [int]$_.Exception.Response.StatusCode.value__ }
Log 13 "EMPLEADO - Eliminar producto (403)" $s "403"

$tG = Get-Token "gerente1" "gerente123"
Log 14 "Login GERENTE" 200 "200"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/usuarios" -Headers (AuthHeader $tG) -ErrorAction Stop }
Log 15 "GERENTE - Listar usuarios" $s "200"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/usuarios" -Headers (AuthHeader $tC) -ErrorAction Stop }
Log 16 "CLIENTE - Listar usuarios (403)" $s "403"

$usuariosJson = (Invoke-WebRequest -Uri "$base/api/usuarios" -Headers (AuthHeader $tG) -UseBasicParsing).Content | ConvertFrom-Json
$clienteId = ($usuariosJson | Where-Object { $_.username -eq "cliente1" } | Select-Object -First 1).id
$tC = Get-Token "cliente1" "cliente123"
$carBody = "{`"usuario`":{`"id`":$clienteId},`"producto`":{`"id`":$prodId},`"cantidad`":2}"
$s = Try-Status { Invoke-RestMethod -Uri "$base/api/carrito" -Method POST -Headers (AuthHeader $tC) -ContentType "application/json" -Body $carBody -ErrorAction Stop }
Log 17 "CLIENTE - Agregar al carrito" $s "200"

$ventaBody = "{`"usuario`":{`"id`":$clienteId},`"fecha`":`"2026-06-01T14:00:00.000Z`"}"
try {
    $venta = Invoke-RestMethod -Uri "$base/api/ventas" -Method POST -Headers (AuthHeader $tC) -ContentType "application/json" -Body $ventaBody
    Log 18 "CLIENTE - Crear venta" 200 "200" "ventaId=$($venta.id)"
    $ventaId = $venta.id
} catch {
    Log 18 "CLIENTE - Crear venta" ([int]$_.Exception.Response.StatusCode.value__) "200"
    $ventaId = 1
}

$detBody = "{`"venta`":{`"id`":$ventaId},`"producto`":{`"id`":$prodId},`"cantidad`":1,`"precio`":500}"
$s = Try-Status { Invoke-RestMethod -Uri "$base/api/detalle-ventas" -Method POST -Headers (AuthHeader $tC) -ContentType "application/json" -Body $detBody -ErrorAction Stop }
Log 19 "CLIENTE - Detalle venta" $s "200"

$tG = Get-Token "gerente1" "gerente123"
# Producto sin referencias (inventario/carrito) para poder eliminarlo
$prodOrfan = Invoke-RestMethod -Uri "$base/api/productos" -Method POST -Headers (AuthHeader $tE) -ContentType "application/json" -Body "{`"nombre`":`"TempDelete`",`"precio`":99,`"stock`":1,`"categoria`":{`"id`":$catId}}"
try { Invoke-RestMethod -Uri "$base/api/productos/$($prodOrfan.id)" -Method Delete -Headers (AuthHeader $tG) -ErrorAction Stop | Out-Null; $s = 204 }
catch { $s = [int]$_.Exception.Response.StatusCode.value__ }
Log 20 "GERENTE - Eliminar producto" $s "204" "prodId=$($prodOrfan.id)"

$s = Try-Status { Invoke-RestMethod -Uri "$base/api/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"cliente1","password":"wrong"}' -ErrorAction Stop }
Log 21 "Login credenciales invalidas (401)" $s "401"

Write-Host "`n=== RESUMEN ===" -ForegroundColor Cyan
$results | Format-Table -AutoSize
$failed = @($results | Where-Object { $_.Result -eq "FAIL" }).Count
Write-Host "Pasaron: $($results.Count - $failed) / $($results.Count) | Fallaron: $failed"
if ($failed -gt 0) { exit 1 }
