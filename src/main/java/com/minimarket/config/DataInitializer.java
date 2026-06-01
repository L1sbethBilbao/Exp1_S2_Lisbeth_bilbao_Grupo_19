package com.minimarket.config;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.constants.SecurityRoles;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearRolSiNoExiste(SecurityRoles.CLIENTE);
        crearRolSiNoExiste(SecurityRoles.EMPLEADO);
        crearRolSiNoExiste(SecurityRoles.GERENTE);

        crearUsuarioDemo("cliente1", "cliente123", SecurityRoles.CLIENTE);
        crearUsuarioDemo("empleado1", "empleado123", SecurityRoles.EMPLEADO);
        crearUsuarioDemo("gerente1", "gerente123", SecurityRoles.GERENTE);
    }

    private void crearRolSiNoExiste(String nombre) {
        if (rolRepository.findByNombre(nombre).isEmpty()) {
            Rol rol = new Rol();
            rol.setNombre(nombre);
            rolRepository.save(rol);
        }
    }

    private void crearUsuarioDemo(String username, String password, String rolNombre) {
        if (usuarioRepository.findByUsername(username).isPresent()) {
            return;
        }

        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new IllegalStateException("Rol no encontrado: " + rolNombre));

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRoles(Set.of(rol));
        usuarioRepository.save(usuario);
    }
}
