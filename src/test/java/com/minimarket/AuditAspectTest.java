package com.minimarket;

import com.minimarket.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuditAspectTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    @WithMockUser(roles = "GERENTE")
    void listarUsuarios_generaAuditLog() throws Exception {
        long before = auditLogRepository.count();
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
        assertThat(auditLogRepository.count()).isGreaterThan(before);
    }
}
