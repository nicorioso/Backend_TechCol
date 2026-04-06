package com.techgroup.techcop.service.report;

public interface ReportService {

    byte[] exportSalesCsv();

    byte[] exportSalesPdf();
}
