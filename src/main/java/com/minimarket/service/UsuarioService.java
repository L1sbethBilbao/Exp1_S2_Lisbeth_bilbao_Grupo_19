package com.minimarket.service;

import com.minimarket.dto.usuario.UsuarioRequestDTO;
import com.minimarket.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<Usuario> findAll();
    Optional<Usuario> findById(Long id);
    Optional<Usuario> findByUsername(String username);
    Usuario save(Usuario usuario);
    Usuario saveFromDto(UsuarioRequestDTO dto);
    void deleteById(Long id);
}