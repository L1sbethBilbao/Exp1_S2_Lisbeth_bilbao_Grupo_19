package com.minimarket;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.constants.SecurityRoles;
import com.minimarket.security.service.MfaService;
import com.minimarket.security.util.JwtUtil;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MfaAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MfaService mfaService;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @Transactional
    void loginGerenteConMfa_requiereVerificacionTotp() throws Exception {
        String username = "gerente_mfa_test";
        Rol gerente = rolRepository.findByNombre(SecurityRoles.GERENTE).orElseThrow();
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode("gerente123"));
        usuario.setRoles(new HashSet<>(Set.of(gerente)));
        usuario.setRetentionExcluded(true);
        usuarioRepository.save(usuario);

        var setup = mfaService.setupMfa(username);
        String secret = setup.getSecret();
        DefaultCodeGenerator generator = new DefaultCodeGenerator();
        String code = generator.generate(secret, Math.floorDiv(System.currentTimeMillis(), 30000L));
        mfaService.confirmMfa(username, code);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"gerente123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mfaRequired").value(true))
                .andExpect(jsonPath("$.mfaToken").exists());
    }
}
