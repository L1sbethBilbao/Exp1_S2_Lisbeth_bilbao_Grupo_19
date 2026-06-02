package com.minimarket.controller;

import com.minimarket.dto.detalleventa.DetalleVentaRequestDTO;
import com.minimarket.dto.detalleventa.DetalleVentaResponseDTO;
import com.minimarket.mapper.DetalleVentaMapper;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.DetalleVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalle-ventas")
public class DetalleVentaController {

    @Autowired
    private DetalleVentaService detalleVentaService;

    @Autowired
    private DetalleVentaMapper detalleVentaMapper;

    @GetMapping
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public List<DetalleVentaResponseDTO> listarDetalleVentas() {
        return detalleVentaMapper.toResponseList(detalleVentaService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public ResponseEntity<DetalleVentaResponseDTO> obtenerDetalleVentaPorId(@PathVariable Long id) {
        var detalleVenta = detalleVentaService.findById(id);
        return (detalleVenta != null) ? ResponseEntity.ok(detalleVentaMapper.toResponse(detalleVenta))
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public DetalleVentaResponseDTO guardarDetalleVenta(@RequestBody DetalleVentaRequestDTO detalleVentaDto) {
        return detalleVentaMapper.toResponse(
                detalleVentaService.save(detalleVentaMapper.toEntity(detalleVentaDto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public ResponseEntity<DetalleVentaResponseDTO> actualizarDetalleVenta(
            @PathVariable Long id, @RequestBody DetalleVentaRequestDTO detalleVentaDto) {
        var existente = detalleVentaService.findById(id);
        if (existente != null) {
            detalleVentaDto.setId(id);
            return ResponseEntity.ok(detalleVentaMapper.toResponse(
                    detalleVentaService.save(detalleVentaMapper.toEntity(detalleVentaDto))));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public ResponseEntity<Void> eliminarDetalleVenta(@PathVariable Long id) {
        var detalleVenta = detalleVentaService.findById(id);
        if (detalleVenta != null) {
            detalleVentaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
