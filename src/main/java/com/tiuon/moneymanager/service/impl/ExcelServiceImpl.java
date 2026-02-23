package com.tiuon.moneymanager.service.impl;

import com.tiuon.moneymanager.service.IExcelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExcelServiceImpl implements IExcelService {

    @Override
    public byte[] generateExcel(List<?> transactions, String type) throws IOException {
        if (transactions == null || transactions.isEmpty()) {
            throw new IllegalArgumentException("Transaction list cannot be null");
        }

        Workbook workbook = new XSSFWorkbook();
        String sheetName = "income".equalsIgnoreCase(type) ? "Income Report" : "Expense Report";
        Sheet sheet = workbook.createSheet(sheetName);

        // Get the class of the first object
        Class<?> clazz = transactions.get(0).getClass();
        List<Field> fields = getAccessibleFields(clazz).stream()
                .filter(f -> checkRequestedField(f.getName()))
                .toList();

        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);

        // Create header row with S.No
        Row headerRow = sheet.createRow(0);
        Cell snoCell = headerRow.createCell(0);
        snoCell.setCellValue("S.No");
        snoCell.setCellStyle(headerStyle);

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            String fieldName = formatFieldName(field.getName());
            Cell cell = headerRow.createCell(i + 1);
            cell.setCellValue(fieldName);
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        int rowNum = 1;

        for (Object transaction : transactions) {
            Row row = sheet.createRow(rowNum);

            // Add S.No
            row.createCell(0).setCellValue(rowNum);
            // Add data from fields
            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                field.setAccessible(true);

                Cell cell = row.createCell(i + 1);
                try {
                    Object value = field.get(transaction);
                    setCellValue(cell, value);
                } catch (IllegalAccessException e) {
                    cell.setCellValue("N/A");
                }
            }
            rowNum++;
        }

        // Auto-size columns
        for (int i = 0; i <= fields.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Write workbook to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    private boolean checkRequestedField(String fieldName) {
        return !"categoryId".equalsIgnoreCase(fieldName) && !"createdAt".equalsIgnoreCase(fieldName) &&
                !"updatedAt".equalsIgnoreCase(fieldName);
    }

    /**
     * Get all accessible fields from a class, excluding static and transient fields
     */
    private List<Field> getAccessibleFields(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> !java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                .filter(field -> !java.lang.reflect.Modifier.isTransient(field.getModifiers()))
                .collect(Collectors.toList());
    }

    /**
     * Set cell value based on the object type
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Float) {
            cell.setCellValue((Float) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) value).doubleValue());
        } else if (value instanceof LocalDate) {
            cell.setCellValue(value.toString());
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(value.toString());
        } else if (value instanceof java.util.Date) {
            cell.setCellValue((java.util.Date) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * Format field name from camelCase to Title Case
     * Example: categoryName -> Category Name
     */
    private String formatFieldName(String fieldName) {
        // Special cases
        if ("id".equalsIgnoreCase(fieldName)) {
            return "ID";
        }

        // Convert camelCase to Title Case
        String formatted = fieldName.replaceAll("([A-Z])", " $1").trim();
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }

    /**
     * Create header cell style
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Add borders
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);

        return style;
    }
}