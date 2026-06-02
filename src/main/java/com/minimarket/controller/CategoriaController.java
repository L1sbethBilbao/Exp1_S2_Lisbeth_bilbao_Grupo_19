package com.minimarket.controller;

import com.minimarket.dto.categoria.CategoriaRequestDTO;
import com.minimarket.dto.categoria.CategoriaResponseDTO;
import com.minimarket.mapper.CategoriaMapper;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private CategoriaMapper categoriaMapper;

    @GetMapping
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public List<CategoriaResponseDTO> listarCategorias() {
        return categoriaMapper.toResponseList(categoriaService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize(SecurityExpressions.AUTENTICADO)
    public ResponseEntity<CategoriaResponseDTO> obtenerCategoriaPorId(@PathVariable Long id) {
        var categoria = categoriaService.findById(id);
        return (categoria != null) ? ResponseEntity.ok(categoriaMapper.toResponse(categoria))
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public CategoriaResponseDTO guardarCategoria(@RequestBody CategoriaRequestDTO categoriaDto) {
        return categoriaMapper.toResponse(categoriaService.save(categoriaMapper.toEntity(categoriaDto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.EMPLEADO_O_GERENTE)
    public ResponseEntity<CategoriaResponseDTO> actualizarCategoria(
            @PathVariable Long id, @RequestBody CategoriaRequestDTO categoriaDto) {
        var categoriaExistente = categoriaService.findById(id);
        if (categoriaExistente != null) {
            categoriaDto.setId(id);
            return ResponseEntity.ok(categoriaMapper.toResponse(
                    categoriaService.save(categoriaMapper.toEntity(categoriaDto))));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        var categoria = categoriaService.findById(id);
        if (categoria != null) {
            categoriaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
