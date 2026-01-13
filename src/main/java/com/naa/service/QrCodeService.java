package com.naa.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class QrCodeService {

    public byte[] generateQrCodeImage(String text, int size) throws Exception {
        return generateQrCodeImage(text, size, false);
    }

    public byte[] generateQrCodeImage(String text, int size, boolean withLabel) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                text,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
        );

        if (!withLabel) {
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            return baos.toByteArray();
        }

        int textHeight = 25;
        int totalHeight = size + textHeight;

        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        BufferedImage combined = new BufferedImage(
                size,
                totalHeight,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = combined.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, size, totalHeight);
        g.drawImage(qrImage, 0, 0, null);

//        g.setColor(Color.BLACK);
//        g.setFont(new Font("Arial", Font.BOLD, 14));

        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int textWidth = metrics.stringWidth(text);
        int x = (size - textWidth) / 2;

        g.drawString(text, x, size + 18);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(combined, "PNG", baos);
        return baos.toByteArray();
    }

    public String generateQrCodeBase64(String text, int size) throws Exception {
        byte[] imageBytes = generateQrCodeImage(text, size, true);
        return Base64.getEncoder().encodeToString(imageBytes);
    }


    public int calculateOptimalColumns(int qrSize, int totalItems) {
        if (qrSize <= 100) return 8;
        if (qrSize <= 150) return 6;
        if (qrSize <= 200) return 4;
        if (qrSize <= 300) return 3;
        return 2;
    }
}
