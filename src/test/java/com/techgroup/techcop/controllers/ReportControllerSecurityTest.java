package com.techgroup.techcop.controllers;

import com.techgroup.techcop.exception.GlobalExceptionHandler;
import com.techgroup.techcop.security.config.SecurityConfig;
import com.techgroup.techcop.security.jwt.JwtAuthEntryPoint;
import com.techgroup.techcop.security.jwt.JwtAuthenticationFilter;
import com.techgroup.techcop.security.jwt.JwtService;
import com.techgroup.techcop.security.model.CustomUserDetailsService;
import com.techgroup.techcop.security.ratelimit.RateLimitingFilter;
import com.techgroup.techcop.support.TestCorsConfig;
import com.techgroup.techcop.service.report.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RateLimitingFilter.class,
        JwtAuthEntryPoint.class,
        TestCorsConfig.class
})
class ReportControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToExportSalesCsv() throws Exception {
        when(reportService.exportSalesCsv()).thenReturn("pedido,total\n1,100".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(get("/reports/sales/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment;")));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void shouldRejectCustomerRoleForSalesReport() throws Exception {
        mockMvc.perform(get("/reports/sales/csv"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reportService);
    }

    @Test
    void shouldRequireAuthenticationForSalesPdf() throws Exception {
        mockMvc.perform(get("/reports/sales/pdf"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reportService);
    }
}
