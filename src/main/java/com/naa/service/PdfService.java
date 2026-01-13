package com.naa.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PdfService {

    @Autowired
    private QrCodeService qrCodeService;

    @Value("${app.qr.max-batch-size:1000}")
    private int maxBatchSize;

    public byte[] generateQrCodesPdf(int start, int end, int qrSize, Integer columns) throws Exception {
        int totalNumbers = end - start + 1;

        if (totalNumbers > maxBatchSize) {
            throw new IllegalArgumentException(
                    String.format("Слишком много QR-кодов: %d. Максимум: %d", totalNumbers, maxBatchSize)
            );
        }

        int actualColumns = (columns != null && columns > 0)
                ? columns
                : qrCodeService.calculateOptimalColumns(qrSize, totalNumbers);

        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        Paragraph title = new Paragraph(
                String.format("QR-коды с числами от %d до %d", start, end),
                titleFont
        );
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        Font infoFont = new Font(Font.FontFamily.HELVETICA, 10);
        Paragraph info = new Paragraph(
                String.format("Всего QR-кодов: %d | Размер QR: %dpx | Колонок: %d",
                        totalNumbers, qrSize, actualColumns),
                infoFont
        );
        info.setAlignment(Element.ALIGN_CENTER);
        info.setSpacingAfter(30);
        document.add(info);

        float pageWidth = document.getPageSize().getWidth() - 100; //TODO Отступы по 50px с каждой стороны
        float pageHeight = document.getPageSize().getHeight() - 150;

        float cellWidth = pageWidth / actualColumns;
        float cellHeight = cellWidth * 1.1f; //TODO Немного выше для текста

        int currentX = 50;
        int currentY = (int) (pageHeight + 50 - cellHeight);
        int currentColumn = 0;

        AtomicInteger counter = new AtomicInteger(start);

        for (int i = start; i <= end; i++) {
            int currentNumber = counter.getAndIncrement();
            String text = String.valueOf(currentNumber);
            byte[] qrImageBytes = qrCodeService.generateQrCodeImage(text, qrSize, true);

            Image qrImage = Image.getInstance(qrImageBytes);
            qrImage.scaleToFit((int)(cellWidth * 0.85), (int)(cellHeight * 0.85));

            float x = currentX + (cellWidth - qrImage.getScaledWidth()) / 2;
            float y = currentY + (cellHeight - qrImage.getScaledHeight()) / 2;

            qrImage.setAbsolutePosition(x, y);
            document.add(qrImage);

            // Добавляем номер под QR-кодом (дублируем, если в самом QR нет текста)
            if (!qrImageBytes.toString().contains(text)) {
                PdfContentByte canvas = writer.getDirectContent();
                canvas.beginText();
                canvas.setFontAndSize(BaseFont.createFont(), 12);
                float textX = currentX + cellWidth / 2;
                float textY = currentY - 5;
                canvas.showTextAligned(
                        Element.ALIGN_CENTER,
                        text,
                        textX,
                        textY,
                        0
                );
                canvas.endText();
            }

            currentColumn++;
            if (currentColumn >= actualColumns) {
                currentColumn = 0;
                currentX = 50;
                currentY -= cellHeight;

                if (currentY < 100) {
                    document.newPage();
                    currentY = (int) (pageHeight + 50 - cellHeight);
                }
            } else {
                currentX += cellWidth;
            }
        }

        document.newPage();
        Paragraph footer = new Paragraph(
                String.format("Документ сгенерирован: %s | Всего QR-кодов: %d",
                        new java.util.Date(), totalNumbers),
                new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC)
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }
}