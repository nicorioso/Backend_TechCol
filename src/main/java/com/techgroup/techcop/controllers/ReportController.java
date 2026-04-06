package com.techgroup.techcop.controllers;

import com.techgroup.techcop.service.report.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/reports")
@Tag(name = "Reports", description = "Exportacion administrativa de reportes de ventas")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Exportar ventas en CSV")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/sales/csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportSalesCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, buildDisposition("sales-report", "csv"))
                .contentType(new MediaType("text", "csv"))
                .body(reportService.exportSalesCsv());
    }

    @Operation(summary = "Exportar ventas en PDF")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/sales/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportSalesPdf() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, buildDisposition("sales-report", "pdf"))
                .contentType(MediaType.APPLICATION_PDF)
                .body(reportService.exportSalesPdf());
    }

    private String buildDisposition(String baseFileName, String extension) {
        String fileName = baseFileName + "-" + LocalDateTime.now().format(FILE_DATE_FORMATTER) + "." + extension;
        return ContentDisposition.attachment().filename(fileName).build().toString();
    }
}
