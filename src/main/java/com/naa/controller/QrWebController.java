package com.naa.controller;

import com.naa.dto.QrGenerationRequest;
import com.naa.service.PdfService;
import com.naa.service.QrCodeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class QrWebController {

    @Autowired
    private PdfService pdfService;

    @Autowired
    private QrCodeService qrCodeService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("request", new QrGenerationRequest());
        return "index";
    }

    @PostMapping("/generate")
    public String generate(
            @Valid @ModelAttribute("request") QrGenerationRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "index";
        }

        try {
            List<String> qrCodes = new ArrayList<>();
            List<Integer> numbers = new ArrayList<>();

            for (int i = request.getStart(); i <= request.getEnd(); i++) {
                String base64Qr = qrCodeService.generateQrCodeBase64(
                        String.valueOf(i),
                        request.getQrSize()
                );
                qrCodes.add(base64Qr);
                numbers.add(i);
            }
            model.addAttribute("qrCodes", qrCodes);
            model.addAttribute("numbers", numbers);
            model.addAttribute("total", qrCodes.size());
            model.addAttribute("start", request.getStart());
            model.addAttribute("end", request.getEnd());

            int htmlColumns = request.isAutoColumns()
                    ? qrCodeService.calculateOptimalColumns(request.getQrSize(), qrCodes.size())
                    : Math.min(request.getColumns(), 8); // Ограничиваем для HTML

            model.addAttribute("columns", htmlColumns);
            return "preview";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка генерации: " + e.getMessage());
            return "index";
        }
    }

    @GetMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @RequestParam int start,
            @RequestParam int end,
            @RequestParam(defaultValue = "150") int size,
            @RequestParam(required = false) Integer columns
    ) {
        try {
            byte[] pdfBytes = pdfService.generateQrCodesPdf(start, end, size, columns);

            String filename = String.format("qr-codes-%d-to-%d.pdf", start, end);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
