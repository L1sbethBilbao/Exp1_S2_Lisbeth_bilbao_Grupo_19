package com.minimarket.service.impl;

import com.minimarket.dto.usuario.UsuarioRequestDTO;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.mapper.UsuarioMapper;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioMapper usuarioMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    public Usuario save(Usuario usuario) {
        if (usuario.getId() != null) {
            usuarioRepository.findById(usuario.getId()).ifPresent(existing -> {
                if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                    usuario.setPassword(existing.getPassword());
                }
                usuario.setTotpSecret(existing.getTotpSecret());
                usuario.setMfaEnabled(existing.isMfaEnabled());
                usuario.setMfaEnrolledAt(existing.getMfaEnrolledAt());
                usuario.setLastLoginAt(existing.getLastLoginAt());
                usuario.setAnonymized(existing.isAnonymized());
                usuario.setRetentionExcluded(existing.isRetentionExcluded());
            });
        } else if (usuario.getLastLoginAt() == null) {
            usuario.setLastLoginAt(LocalDateTime.now());
        }
        if (usuario.getPassword() != null && !usuario.getPassword().isBlank()
                && !isBcryptHash(usuario.getPassword())) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario saveFromDto(UsuarioRequestDTO dto) {
        Usuario usuario = usuarioMapper.toEntity(dto);
        if (dto.getRoleNames() != null && !dto.getRoleNames().isEmpty()) {
            Set<Rol> roles = new HashSet<>();
            for (String roleName : dto.getRoleNames()) {
                Rol rol = rolRepository.findByNombre(roleName)
                        .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + roleName));
                roles.add(rol);
            }
            usuario.setRoles(roles);
        }
        return save(usuario);
    }

    private boolean isBcryptHash(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }

    @Override
    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }
}
