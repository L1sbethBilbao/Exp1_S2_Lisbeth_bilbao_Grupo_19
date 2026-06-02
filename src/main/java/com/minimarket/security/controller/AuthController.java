package com.minimarket.security.controller;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.constants.SecurityRoles;
import com.minimarket.security.model.*;
import com.minimarket.security.service.AuthService;
import com.minimarket.security.service.LoginAttemptService;
import com.minimarket.security.service.MfaService;
import com.minimarket.security.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private MfaService mfaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        loginAttemptService.checkNotBlocked(request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            loginAttemptService.loginSucceeded(request.getUsername());

            Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
            usuario.setLastLoginAt(LocalDateTime.now());
            usuarioRepository.save(usuario);

            if (mfaService.isGerenteWithMfa(usuario)) {
                String mfaToken = jwtUtil.generateMfaToken(usuario.getUsername());
                return ResponseEntity.ok(JwtResponse.mfaChallenge(mfaToken));
            }

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (BadCredentialsException ex) {
            loginAttemptService.loginFailed(request.getUsername());
            throw ex;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/mfa/setup")
    @PreAuthorize("hasRole('" + SecurityRoles.GERENTE + "')")
    public ResponseEntity<MfaSetupResponse> setupMfa() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(mfaService.setupMfa(username));
    }

    @PostMapping("/mfa/confirm")
    @PreAuthorize("hasRole('" + SecurityRoles.GERENTE + "')")
    public ResponseEntity<MapMessage> confirmMfa(@Valid @RequestBody MfaConfirmRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        mfaService.confirmMfa(username, request.getCode());
        return ResponseEntity.ok(new MapMessage("MFA activado correctamente"));
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<JwtResponse> verifyMfa(@Valid @RequestBody MfaVerifyRequest request) {
        if (!jwtUtil.isMfaToken(request.getMfaToken())) {
            throw new BadCredentialsException("Token MFA inválido");
        }
        String username = jwtUtil.extractUsername(request.getMfaToken());
        if (!jwtUtil.validateToken(request.getMfaToken(), username)) {
            throw new BadCredentialsException("Token MFA expirado");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        if (!mfaService.verifyCode(usuario, request.getCode())) {
            throw new BadCredentialsException("Código TOTP inválido");
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(usuario.getRoles().stream()
                        .map(r -> SecurityRoles.toAuthority(r.getNombre()))
                        .toArray(String[]::new))
                .build();

        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    public record MapMessage(String message) {
    }
}
