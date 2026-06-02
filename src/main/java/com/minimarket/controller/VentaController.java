package com.minimarket.controller;

import com.minimarket.dto.venta.VentaRequestDTO;
import com.minimarket.dto.venta.VentaResponseDTO;
import com.minimarket.mapper.VentaMapper;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private VentaMapper ventaMapper;

    @GetMapping
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public List<VentaResponseDTO> listarVentas() {
        return ventaMapper.toResponseList(ventaService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public VentaResponseDTO obtenerVentaPorId(@PathVariable Long id) {
        var venta = ventaService.findById(id);
        return venta != null ? ventaMapper.toResponse(venta) : null;
    }

    @PostMapping
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public VentaResponseDTO guardarVenta(@RequestBody VentaRequestDTO ventaDto) {
        return ventaMapper.toResponse(ventaService.save(ventaMapper.toEntity(ventaDto)));
    }
}
