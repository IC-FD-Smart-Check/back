package org.fdsmartcheck.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.fdsmartcheck.exception.ResourceNotFoundException;
import org.fdsmartcheck.model.Check;
import org.fdsmartcheck.model.Event;
import org.fdsmartcheck.model.SubEvent;
import org.fdsmartcheck.repository.CheckRepository;
import org.fdsmartcheck.repository.EventRepository;
import org.fdsmartcheck.repository.SubEventRepository;
import org.fdsmartcheck.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelReportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EventRepository eventRepository;
    private final SubEventRepository subEventRepository;
    private final CheckRepository checkRepository;
    private final SubscriptionRepository subscriptionRepository;

    public byte[] generateExcelReport(String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));

        List<SubEvent> subEvents = subEventRepository.findByEventId(eventId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Relatorio");

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle labelStyle = createLabelStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle subEventTitleStyle = createSubEventTitleStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            int rowIdx = 0;

            Row titleRow = sheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("RELATÓRIO DE PRESENÇA");
            titleCell.setCellStyle(titleStyle);

            Row eventRow = sheet.createRow(rowIdx++);
            eventRow.createCell(0).setCellValue("Evento:");
            eventRow.getCell(0).setCellStyle(labelStyle);
            eventRow.createCell(1).setCellValue(event.getTitle());

            Row periodRow = sheet.createRow(rowIdx++);
            periodRow.createCell(0).setCellValue("Período:");
            periodRow.getCell(0).setCellStyle(labelStyle);
            periodRow.createCell(1).setCellValue(
                    event.getStartDate().format(DATE_FORMATTER) + " a " + event.getEndDate().format(DATE_FORMATTER));

            Row generatedRow = sheet.createRow(rowIdx++);
            generatedRow.createCell(0).setCellValue("Gerado em:");
            generatedRow.getCell(0).setCellStyle(labelStyle);
            generatedRow.createCell(1).setCellValue(LocalDateTime.now().format(DATE_TIME_FORMATTER));

            rowIdx++;

            boolean isFirstSubEvent = true;
            for (SubEvent subEvent : subEvents) {
                if (!isFirstSubEvent) {
                    rowIdx++;
                }
                isFirstSubEvent = false;

                Row subEventTitleRow = sheet.createRow(rowIdx++);
                org.apache.poi.ss.usermodel.Cell subEventCell = subEventTitleRow.createCell(0);
                subEventCell.setCellValue(subEvent.getTitle() + " (" + subEvent.getStartDate().format(DATE_FORMATTER) + ")");
                subEventCell.setCellStyle(subEventTitleStyle);

                List<Check> checks = checkRepository.findBySubEventId(subEvent.getId());
                long totalSubscriptions = subscriptionRepository.findBySubEventId(subEvent.getId()).size();
                long totalChecks = checks.size();
                long full = checks.stream().filter(this::hasCheckinAndCheckout).count();
                long onlyCheckin = checks.stream().filter(this::hasOnlyCheckin).count();
                long onlyCheckout = checks.stream().filter(this::hasOnlyCheckout).count();
                long none = checks.stream().filter(this::hasNoCheckinOrCheckout).count();

                Row subscriptionsRow = sheet.createRow(rowIdx++);
                subscriptionsRow.createCell(0).setCellValue("Inscritos: " + totalSubscriptions);
                subscriptionsRow.getCell(0).setCellStyle(labelStyle);

                Row totalChecksRow = sheet.createRow(rowIdx++);
                totalChecksRow.createCell(0).setCellValue("Registros: " + totalChecks);
                totalChecksRow.getCell(0).setCellStyle(labelStyle);

                Row fullRow = sheet.createRow(rowIdx++);
                fullRow.createCell(0).setCellValue("Check-in e Check-out: " + full);
                fullRow.getCell(0).setCellStyle(labelStyle);

                Row onlyCheckinRow = sheet.createRow(rowIdx++);
                onlyCheckinRow.createCell(0).setCellValue("Somente Check-in: " + onlyCheckin);
                onlyCheckinRow.getCell(0).setCellStyle(labelStyle);

                Row onlyCheckoutRow = sheet.createRow(rowIdx++);
                onlyCheckoutRow.createCell(0).setCellValue("Somente Check-out: " + onlyCheckout);
                onlyCheckoutRow.getCell(0).setCellStyle(labelStyle);

                Row noneRow = sheet.createRow(rowIdx++);
                noneRow.createCell(0).setCellValue("Sem Check-in/Check-out: " + none);
                noneRow.getCell(0).setCellStyle(labelStyle);

                Row headerRow = sheet.createRow(rowIdx++);
                createHeaderCell(headerRow, 0, "Nome", headerStyle);
                createHeaderCell(headerRow, 1, "Email", headerStyle);
                createHeaderCell(headerRow, 2, "Check-in", headerStyle);
                createHeaderCell(headerRow, 3, "Check-out", headerStyle);

                if (checks.isEmpty()) {
                    Row emptyRow = sheet.createRow(rowIdx++);
                    createDataCell(emptyRow, 0, "Nenhum registro de presença", dataStyle);
                } else {
                    for (Check check : checks) {
                        Row row = sheet.createRow(rowIdx++);
                        createDataCell(row, 0, check.getUser().getName(), dataStyle);
                        createDataCell(row, 1, check.getUser().getEmail(), dataStyle);
                        createDataCell(row, 2, check.getCheckinTime() != null
                                ? check.getCheckinTime().format(DATE_TIME_FORMATTER) : "-", dataStyle);
                        createDataCell(row, 3, check.getCheckoutTime() != null
                                ? check.getCheckoutTime().format(DATE_TIME_FORMATTER) : "-", dataStyle);
                    }
                }
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório Excel", e);
        }
    }

    public byte[] generateSubEventExcelReport(String subEventId) {
        SubEvent subEvent = subEventRepository.findById(subEventId)
                .orElseThrow(() -> new ResourceNotFoundException("Subevento não encontrado"));

        Event event = subEvent.getEvent();
        List<Check> checks = checkRepository.findBySubEventId(subEventId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Relatorio");

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle labelStyle = createLabelStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            int rowIdx = 0;

            Row titleRow = sheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("RELATÓRIO DE PRESENÇA");
            titleCell.setCellStyle(titleStyle);

            Row eventRow = sheet.createRow(rowIdx++);
            eventRow.createCell(0).setCellValue("Evento:");
            eventRow.getCell(0).setCellStyle(labelStyle);
            eventRow.createCell(1).setCellValue(event.getTitle());

            Row subEventRow = sheet.createRow(rowIdx++);
            subEventRow.createCell(0).setCellValue("Subevento:");
            subEventRow.getCell(0).setCellStyle(labelStyle);
            subEventRow.createCell(1).setCellValue(subEvent.getTitle());

            Row periodRow = sheet.createRow(rowIdx++);
            periodRow.createCell(0).setCellValue("Data:");
            periodRow.getCell(0).setCellStyle(labelStyle);
            periodRow.createCell(1).setCellValue(
                    subEvent.getStartDate().format(DATE_TIME_FORMATTER) + " a " +
                            subEvent.getEndDate().format(DATE_TIME_FORMATTER)
            );

            Row generatedRow = sheet.createRow(rowIdx++);
            generatedRow.createCell(0).setCellValue("Gerado em:");
            generatedRow.getCell(0).setCellStyle(labelStyle);
            generatedRow.createCell(1).setCellValue(LocalDateTime.now().format(DATE_TIME_FORMATTER));

            rowIdx++;

            long totalSubscriptions = subscriptionRepository.findBySubEventId(subEventId).size();
            Row subscriptionsRow = sheet.createRow(rowIdx++);
            subscriptionsRow.createCell(0).setCellValue("Inscritos: " + totalSubscriptions);
            subscriptionsRow.getCell(0).setCellStyle(labelStyle);

            long totalChecks = checks.size();
            long full = checks.stream().filter(this::hasCheckinAndCheckout).count();
            long onlyCheckin = checks.stream().filter(this::hasOnlyCheckin).count();
            long onlyCheckout = checks.stream().filter(this::hasOnlyCheckout).count();
            long none = checks.stream().filter(this::hasNoCheckinOrCheckout).count();

            Row totalChecksRow = sheet.createRow(rowIdx++);
            totalChecksRow.createCell(0).setCellValue("Registros: " + totalChecks);
            totalChecksRow.getCell(0).setCellStyle(labelStyle);

            Row fullRow = sheet.createRow(rowIdx++);
            fullRow.createCell(0).setCellValue("Check-in e Check-out: " + full);
            fullRow.getCell(0).setCellStyle(labelStyle);

            Row onlyCheckinRow = sheet.createRow(rowIdx++);
            onlyCheckinRow.createCell(0).setCellValue("Somente Check-in: " + onlyCheckin);
            onlyCheckinRow.getCell(0).setCellStyle(labelStyle);

            Row onlyCheckoutRow = sheet.createRow(rowIdx++);
            onlyCheckoutRow.createCell(0).setCellValue("Somente Check-out: " + onlyCheckout);
            onlyCheckoutRow.getCell(0).setCellStyle(labelStyle);

            Row noneRow = sheet.createRow(rowIdx++);
            noneRow.createCell(0).setCellValue("Sem Check-in/Check-out: " + none);
            noneRow.getCell(0).setCellStyle(labelStyle);

            Row headerRow = sheet.createRow(rowIdx++);
            createHeaderCell(headerRow, 0, "Nome", headerStyle);
            createHeaderCell(headerRow, 1, "Email", headerStyle);
            createHeaderCell(headerRow, 2, "Check-in", headerStyle);
            createHeaderCell(headerRow, 3, "Check-out", headerStyle);

            for (Check check : checks) {
                Row row = sheet.createRow(rowIdx++);
                createDataCell(row, 0, check.getUser().getName(), dataStyle);
                createDataCell(row, 1, check.getUser().getEmail(), dataStyle);
                createDataCell(row, 2, check.getCheckinTime() != null
                        ? check.getCheckinTime().format(DATE_TIME_FORMATTER) : "-", dataStyle);
                createDataCell(row, 3, check.getCheckoutTime() != null
                        ? check.getCheckoutTime().format(DATE_TIME_FORMATTER) : "-", dataStyle);
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório Excel", e);
        }
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private CellStyle createLabelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createSubEventTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void createHeaderCell(Row row, int index, String value, CellStyle style) {
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(index);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createDataCell(Row row, int index, String value, CellStyle style) {
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(index);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private boolean hasCheckinAndCheckout(Check check) {
        return check.getCheckinTime() != null && check.getCheckoutTime() != null;
    }

    private boolean hasOnlyCheckin(Check check) {
        return check.getCheckinTime() != null && check.getCheckoutTime() == null;
    }

    private boolean hasOnlyCheckout(Check check) {
        return check.getCheckinTime() == null && check.getCheckoutTime() != null;
    }

    private boolean hasNoCheckinOrCheckout(Check check) {
        return check.getCheckinTime() == null && check.getCheckoutTime() == null;
    }
}