package com.oss.osscourse.util;

import com.oss.osscourse.common.BusinessException;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ImportSheetReader {

    private ImportSheetReader() {
    }

    public static List<ImportRowData> readDataRows(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new BusinessException(400, "文件名不能为空");
        }

        String lowerName = filename.toLowerCase(Locale.ROOT);
        try {
            if (lowerName.endsWith(".csv")) {
                return readCsvRows(file);
            }
            if (lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")) {
                return readWorkbookRows(file);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(400, "Excel文件解析失败: " + e.getMessage());
        }

        throw new BusinessException(400, "仅支持 .xlsx、.xls、.csv 格式");
    }

    private static List<ImportRowData> readWorkbookRows(MultipartFile file) throws Exception {
        List<ImportRowData> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getLastRowNum() < 1) {
                throw new BusinessException(400, "Excel文件无数据行");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                List<String> values = extractWorkbookRowValues(row);
                rows.add(new ImportRowData(i + 1, values));
            }
        }
        return rows;
    }

    private static List<ImportRowData> readCsvRows(MultipartFile file) throws IOException {
        List<ImportRowData> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int rowNumber = 0;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (rowNumber == 1) {
                    continue;
                }
                rows.add(new ImportRowData(rowNumber, parseCsvLine(stripBom(line))));
            }
        }

        if (rows.isEmpty()) {
            throw new BusinessException(400, "Excel文件无数据行");
        }
        return rows;
    }

    private static List<String> extractWorkbookRowValues(Row row) {
        List<String> values = new ArrayList<>();
        if (row == null) {
            return values;
        }

        short lastCellNum = row.getLastCellNum();
        if (lastCellNum < 0) {
            return values;
        }

        for (int index = 0; index < lastCellNum; index++) {
            values.add(getCellStringValue(row.getCell(index)));
        }
        return values;
    }

    private static String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double numericValue = cell.getNumericCellValue();
            if (numericValue == Math.floor(numericValue) && !Double.isInfinite(numericValue)) {
                return String.valueOf((long) numericValue);
            }
            return String.valueOf(numericValue);
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue() == null ? null : cell.getStringCellValue().trim();
        }
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue() == null ? null : cell.getStringCellValue().trim();
    }

    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }

            if (ch == ',' && !inQuotes) {
                values.add(normalizeCsvValue(current.toString()));
                current.setLength(0);
                continue;
            }

            current.append(ch);
        }

        values.add(normalizeCsvValue(current.toString()));
        return values;
    }

    private static String normalizeCsvValue(String raw) {
        String value = raw == null ? null : raw.trim();
        return value == null || value.isEmpty() ? null : value;
    }

    private static String stripBom(String line) {
        if (line != null && !line.isEmpty() && line.charAt(0) == '\uFEFF') {
            return line.substring(1);
        }
        return line;
    }

    @Getter
    public static class ImportRowData {
        private final int rowNumber;
        private final List<String> cells;

        public ImportRowData(int rowNumber, List<String> cells) {
            this.rowNumber = rowNumber;
            this.cells = cells == null ? List.of() : cells;
        }

        public String getCell(int index) {
            if (index < 0 || index >= cells.size()) {
                return null;
            }
            return cells.get(index);
        }

        public boolean isEmpty() {
            for (String value : cells) {
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
            return true;
        }
    }
}
