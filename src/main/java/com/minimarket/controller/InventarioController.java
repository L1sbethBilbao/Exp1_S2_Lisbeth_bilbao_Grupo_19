package com.minimarket.controller;

import com.minimarket.dto.inventario.InventarioRequestDTO;
import com.minimarket.dto.inventario.InventarioResponseDTO;
import com.minimarket.mapper.InventarioMapper;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private InventarioMapper inventarioMapper;

    @GetMapping
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public List<InventarioResponseDTO> listarMovimientosDeInventario() {
        return inventarioMapper.toResponseList(inventarioService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public ResponseEntity<InventarioResponseDTO> obtenerMovimientoPorId(@PathVariable Long id) {
        var inventario = inventarioService.findById(id);
        return (inventario != null) ? ResponseEntity.ok(inventarioMapper.toResponse(inventario))
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public InventarioResponseDTO registrarMovimiento(@RequestBody InventarioRequestDTO inventarioDto) {
        return inventarioMapper.toResponse(inventarioService.save(inventarioMapper.toEntity(inventarioDto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public ResponseEntity<InventarioResponseDTO> actualizarMovimiento(
            @PathVariable Long id, @RequestBody InventarioRequestDTO inventarioDto) {
        var existente = inventarioService.findById(id);
        if (existente != null) {
            inventarioDto.setId(id);
            return ResponseEntity.ok(inventarioMapper.toResponse(
                    inventarioService.save(inventarioMapper.toEntity(inventarioDto))));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Long id) {
        var inventario = inventarioService.findById(id);
        if (inventario != null) {
            inventarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
