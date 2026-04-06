package com.techgroup.techcop.service.report.impl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.techgroup.techcop.model.entity.Orders;
import com.techgroup.techcop.repository.OrderRepository;
import com.techgroup.techcop.service.report.ReportService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final OrderRepository orderRepository;

    public ReportServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public byte[] exportSalesCsv() {
        List<Orders> orders = orderRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("order_id,order_date,customer,email,status,paypal_order_id,item_count,total").append('\n');

        for (Orders order : orders) {
            csv.append(csvValue(order.getOrderId()))
                    .append(',')
                    .append(csvValue(formatDate(order)))
                    .append(',')
                    .append(csvValue(resolveCustomerName(order)))
                    .append(',')
                    .append(csvValue(resolveCustomerEmail(order)))
                    .append(',')
                    .append(csvValue(order.getStatus()))
                    .append(',')
                    .append(csvValue(order.getPaypalOrderId()))
                    .append(',')
                    .append(csvValue(order.getOrderDetails() == null ? 0 : order.getOrderDetails().size()))
                    .append(',')
                    .append(csvValue(order.getOrderPrice()))
                    .append('\n');
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportSalesPdf() {
        List<Orders> orders = orderRepository.findAll();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font subtitleFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font bodyFont = new Font(Font.HELVETICA, 9, Font.NORMAL);

            document.add(new Paragraph("Reporte de ventas - TechCol", titleFont));
            document.add(new Paragraph("Exportacion administrativa de pedidos registrados.", subtitleFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(new float[]{1.2f, 1.8f, 2.2f, 2.4f, 1.4f, 2.1f, 1.1f, 1.4f});
            table.setWidthPercentage(100);

            addHeaderCell(table, "Pedido", headerFont);
            addHeaderCell(table, "Fecha", headerFont);
            addHeaderCell(table, "Cliente", headerFont);
            addHeaderCell(table, "Correo", headerFont);
            addHeaderCell(table, "Estado", headerFont);
            addHeaderCell(table, "PayPal", headerFont);
            addHeaderCell(table, "Items", headerFont);
            addHeaderCell(table, "Total", headerFont);

            for (Orders order : orders) {
                addBodyCell(table, String.valueOf(order.getOrderId()), bodyFont);
                addBodyCell(table, formatDate(order), bodyFont);
                addBodyCell(table, resolveCustomerName(order), bodyFont);
                addBodyCell(table, resolveCustomerEmail(order), bodyFont);
                addBodyCell(table, safeValue(order.getStatus()), bodyFont);
                addBodyCell(table, safeValue(order.getPaypalOrderId()), bodyFont);
                addBodyCell(table, String.valueOf(order.getOrderDetails() == null ? 0 : order.getOrderDetails().size()), bodyFont);
                addBodyCell(table, formatAmount(order.getOrderPrice()), bodyFont);
            }

            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total de pedidos exportados: " + orders.size(), subtitleFont));
        } catch (DocumentException exception) {
            throw new IllegalStateException("No fue posible generar el reporte PDF de ventas", exception);
        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    private void addHeaderCell(PdfPTable table, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(safeValue(value), font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String resolveCustomerName(Orders order) {
        if (order.getCustomer() == null) {
            return "-";
        }

        String firstName = safeValue(order.getCustomer().getCustomerName());
        String lastName = safeValue(order.getCustomer().getCustomerLastName());
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty() ? "-" : fullName;
    }

    private String resolveCustomerEmail(Orders order) {
        return order.getCustomer() == null ? "-" : safeValue(order.getCustomer().getCustomerEmail());
    }

    private String formatDate(Orders order) {
        if (order.getOrderDate() != null) {
            return order.getOrderDate().format(DATE_TIME_FORMATTER);
        }

        if (order.getCreatedAt() != null) {
            return order.getCreatedAt().format(DATE_TIME_FORMATTER);
        }

        return "-";
    }

    private String formatAmount(BigDecimal amount) {
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;
        return String.format(Locale.US, "%.2f", safeAmount);
    }

    private String csvValue(Object value) {
        String normalized = safeValue(value == null ? "" : value.toString()).replace("\"", "\"\"");
        return "\"" + normalized + "\"";
    }

    private String safeValue(Object value) {
        return value == null ? "-" : value.toString().trim();
    }
}
