package org.fdsmartcheck.service;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
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
public class PdfReportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EventRepository eventRepository;
    private final SubEventRepository subEventRepository;
    private final CheckRepository checkRepository;
    private final SubscriptionRepository subscriptionRepository;

    public byte[] generatePdfReport(String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));

        List<SubEvent> subEvents = subEventRepository.findByEventId(eventId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            addHeader(document, event);

            for (SubEvent subEvent : subEvents) {
                List<Check> checks = checkRepository.findBySubEventId(subEvent.getId());
                addSubEventSection(document, subEvent, checks);
            }

            addGeneralStatistics(document, subEvents);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório PDF", e);
        }
    }

    public byte[] generateSubEventPdfReport(String subEventId) {
        SubEvent subEvent = subEventRepository.findById(subEventId)
                .orElseThrow(() -> new ResourceNotFoundException("Subevento não encontrado"));

        Event event = subEvent.getEvent();
        List<Check> checks = checkRepository.findBySubEventId(subEventId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            addHeader(document, event);
            addSubEventSection(document, subEvent, checks);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório PDF", e);
        }
    }

    private void addHeader(Document document, Event event) {
        Paragraph title = new Paragraph("RELATÓRIO DE PRESENÇA")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        document.add(new Paragraph("Evento: " + event.getTitle()).setBold());
        document.add(new Paragraph("Período: " +
                event.getStartDate().format(DATE_FORMATTER) + " a " +
                event.getEndDate().format(DATE_FORMATTER)));
        document.add(new Paragraph("Gerado em: " + LocalDateTime.now().format(DATE_TIME_FORMATTER))
                .setMarginBottom(20));
    }

    private void addSubEventSection(Document document, SubEvent subEvent, List<Check> checks) {
        document.add(new Paragraph("\n" + subEvent.getTitle())
                .setFontSize(14)
                .setBold());

        document.add(new Paragraph("Data: " + subEvent.getStartDate().format(DATE_TIME_FORMATTER) +
                " a " + subEvent.getEndDate().format(DATE_TIME_FORMATTER))
                .setFontSize(10));

        long totalSubscriptions = subscriptionRepository.findBySubEventId(subEvent.getId()).size();
        long totalChecks = checks.size();
        long full = checks.stream().filter(this::hasCheckinAndCheckout).count();
        long onlyCheckin = checks.stream().filter(this::hasOnlyCheckin).count();
        long onlyCheckout = checks.stream().filter(this::hasOnlyCheckout).count();
        long none = checks.stream().filter(this::hasNoCheckinOrCheckout).count();
        document.add(new Paragraph("Inscritos: " + totalSubscriptions).setMarginBottom(2));
        document.add(new Paragraph("Registros: " + totalChecks).setMarginBottom(2));
        document.add(new Paragraph("Check-in e Check-out: " + full));
        document.add(new Paragraph("Somente Check-in: " + onlyCheckin));
        document.add(new Paragraph("Somente Check-out: " + onlyCheckout));
        document.add(new Paragraph("Sem Check-in/Check-out: " + none).setMarginBottom(10));

        if (checks.isEmpty()) {
            document.add(new Paragraph("Nenhum registro de presença.\n"));
            return;
        }

        Table table = new Table(new float[]{3, 3, 2, 2});
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(15);

        table.addHeaderCell(createHeaderCell("Nome"));
        table.addHeaderCell(createHeaderCell("Email"));
        table.addHeaderCell(createHeaderCell("Check-in"));
        table.addHeaderCell(createHeaderCell("Check-out"));

        checks.forEach(check -> {
            table.addCell(createCell(check.getUser().getName()));
            table.addCell(createCell(check.getUser().getEmail()));
            table.addCell(createCell(check.getCheckinTime() != null
                    ? check.getCheckinTime().format(DATE_TIME_FORMATTER) : "-"));
            table.addCell(createCell(check.getCheckoutTime() != null
                    ? check.getCheckoutTime().format(DATE_TIME_FORMATTER) : "-"));
        });

        document.add(table);
    }

    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(new DeviceRgb(220, 220, 220))
                .setPadding(5);
    }

    private Cell createCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setPadding(5)
                .setFontSize(9);
    }

    private void addGeneralStatistics(Document document, List<SubEvent> subEvents) {
        document.add(new Paragraph("\nESTATÍSTICAS GERAIS")
                .setFontSize(14)
                .setBold()
                .setMarginTop(10));

        int totalSubEvents = subEvents.size();
        long totalSubscriptions = 0;
        long totalChecks = 0;
        long totalFull = 0;
        long totalOnlyCheckin = 0;
        long totalOnlyCheckout = 0;
        long totalNone = 0;

        for (SubEvent subEvent : subEvents) {
            totalSubscriptions += subscriptionRepository.findBySubEventId(subEvent.getId()).size();
            List<Check> checks = checkRepository.findBySubEventId(subEvent.getId());
            totalChecks += checks.size();
            totalFull += checks.stream().filter(this::hasCheckinAndCheckout).count();
            totalOnlyCheckin += checks.stream().filter(this::hasOnlyCheckin).count();
            totalOnlyCheckout += checks.stream().filter(this::hasOnlyCheckout).count();
            totalNone += checks.stream().filter(this::hasNoCheckinOrCheckout).count();
        }

        document.add(new Paragraph("Total de Subeventos: " + totalSubEvents));
        document.add(new Paragraph("Total de Inscritos: " + totalSubscriptions));
        document.add(new Paragraph("Total de Registros: " + totalChecks));
        document.add(new Paragraph("Check-in e Check-out: " + totalFull));
        document.add(new Paragraph("Somente Check-in: " + totalOnlyCheckin));
        document.add(new Paragraph("Somente Check-out: " + totalOnlyCheckout));
        document.add(new Paragraph("Sem Check-in/Check-out: " + totalNone));

        if (totalChecks > 0) {
            double percentage = (totalFull * 100.0) / totalChecks;
            document.add(new Paragraph("Taxa de Presença (com check-in e check-out): " + String.format("%.2f%%", percentage)));
        }
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