package com.minimarket.controller;

import com.minimarket.dto.usuario.UsuarioRequestDTO;
import com.minimarket.dto.usuario.UsuarioResponseDTO;
import com.minimarket.mapper.UsuarioMapper;
import com.minimarket.security.audit.AuditAction;
import com.minimarket.security.audit.Audited;
import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioMapper usuarioMapper;

    @GetMapping
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    @Audited(action = AuditAction.LIST)
    public List<UsuarioResponseDTO> listarUsuarios() {
        return usuarioMapper.toResponseList(usuarioService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    @Audited(action = AuditAction.READ)
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuarioPorId(@PathVariable Long id) {
        Optional<com.minimarket.entity.Usuario> usuario = usuarioService.findById(id);
        return usuario.map(u -> ResponseEntity.ok(usuarioMapper.toResponse(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    @Audited(action = AuditAction.CREATE)
    public UsuarioResponseDTO guardarUsuario(@RequestBody UsuarioRequestDTO usuarioDto) {
        return usuarioMapper.toResponse(usuarioService.saveFromDto(usuarioDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    @Audited(action = AuditAction.UPDATE)
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(
            @PathVariable Long id, @RequestBody UsuarioRequestDTO usuarioDto) {
        Optional<com.minimarket.entity.Usuario> usuarioExistente = usuarioService.findById(id);
        if (usuarioExistente.isPresent()) {
            usuarioDto.setId(id);
            return ResponseEntity.ok(usuarioMapper.toResponse(usuarioService.saveFromDto(usuarioDto)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    @Audited(action = AuditAction.DELETE)
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        Optional<com.minimarket.entity.Usuario> usuario = usuarioService.findById(id);
        if (usuario.isPresent()) {
            usuarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
