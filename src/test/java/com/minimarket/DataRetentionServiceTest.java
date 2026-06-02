package com.minimarket;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.constants.SecurityRoles;
import com.minimarket.security.retention.DataRetentionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DataRetentionServiceTest {

    @Autowired
    private DataRetentionService dataRetentionService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Transactional
    void usuarioInactivo_esAnonimizado() {
        Rol cliente = rolRepository.findByNombre(SecurityRoles.CLIENTE).orElseThrow();
        Usuario usuario = new Usuario();
        usuario.setUsername("inactive_user_test");
        usuario.setPassword(passwordEncoder.encode("test1234"));
        usuario.setRoles(new HashSet<>(Set.of(cliente)));
        usuario.setLastLoginAt(LocalDateTime.now().minusDays(91));
        usuario.setRetentionExcluded(false);
        usuarioRepository.save(usuario);

        int count = dataRetentionService.anonymizeInactiveUsers();
        assertThat(count).isGreaterThanOrEqualTo(1);

        Usuario updated = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertThat(updated.isAnonymized()).isTrue();
        assertThat(updated.getUsername()).startsWith("anon_");
    }

    @Test
    @Transactional
    void usuarioDemo_noEsAnonimizado() {
        Usuario demo = usuarioRepository.findByUsername("cliente1").orElseThrow();
        assertThat(demo.isRetentionExcluded()).isTrue();
        assertThat(demo.isAnonymized()).isFalse();
    }
}
