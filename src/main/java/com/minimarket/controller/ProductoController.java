package com.minimarket.controller;

import com.minimarket.dto.producto.ProductoRequestDTO;
import com.minimarket.dto.producto.ProductoResponseDTO;
import com.minimarket.mapper.ProductoMapper;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoMapper productoMapper;

    @GetMapping
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public List<ProductoResponseDTO> listarProductos() {
        return productoMapper.toResponseList(productoService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public ResponseEntity<ProductoResponseDTO> obtenerProductoPorId(@PathVariable Long id) {
        var producto = productoService.findById(id);
        return (producto != null) ? ResponseEntity.ok(productoMapper.toResponse(producto))
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public ProductoResponseDTO guardarProducto(@RequestBody ProductoRequestDTO productoDto) {
        return productoMapper.toResponse(productoService.save(productoMapper.toEntity(productoDto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public ResponseEntity<ProductoResponseDTO> actualizarProducto(
            @PathVariable Long id, @RequestBody ProductoRequestDTO productoDto) {
        var productoExistente = productoService.findById(id);
        if (productoExistente != null) {
            productoDto.setId(id);
            return ResponseEntity.ok(productoMapper.toResponse(
                    productoService.save(productoMapper.toEntity(productoDto))));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        var producto = productoService.findById(id);
        if (producto != null) {
            productoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
