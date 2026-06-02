package com.minimarket.controller;

import com.minimarket.dto.carrito.CarritoRequestDTO;
import com.minimarket.dto.carrito.CarritoResponseDTO;
import com.minimarket.mapper.CarritoMapper;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private CarritoMapper carritoMapper;

    @GetMapping
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public List<CarritoResponseDTO> listarCarrito() {
        return carritoMapper.toResponseList(carritoService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public ResponseEntity<CarritoResponseDTO> obtenerCarritoPorId(@PathVariable Long id) {
        var carrito = carritoService.findById(id);
        return (carrito != null) ? ResponseEntity.ok(carritoMapper.toResponse(carrito))
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public CarritoResponseDTO agregarProductoAlCarrito(@RequestBody CarritoRequestDTO carritoDto) {
        return carritoMapper.toResponse(carritoService.save(carritoMapper.toEntity(carritoDto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public ResponseEntity<CarritoResponseDTO> actualizarCarrito(
            @PathVariable Long id, @RequestBody CarritoRequestDTO carritoDto) {
        var existente = carritoService.findById(id);
        if (existente != null) {
            carritoDto.setId(id);
            return ResponseEntity.ok(carritoMapper.toResponse(
                    carritoService.save(carritoMapper.toEntity(carritoDto))));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public ResponseEntity<Void> eliminarProductoDelCarrito(@PathVariable Long id) {
        var carrito = carritoService.findById(id);
        if (carrito != null) {
            carritoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
