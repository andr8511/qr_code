package com.naa.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class QrGenerationRequest {

    @NotNull(message = "Начальное число обязательно")
    @Min(value = 1, message = "Число должно быть не менее 1")
    private Integer start;

    @NotNull(message = "Конечное число обязательно")
    @Min(value = 1, message = "Число должно быть не менее 1")
    private Integer end;

    @Min(value = 50, message = "Минимальный размер QR-кода: 50px")
    @Max(value = 500, message = "Максимальный размер QR-кода: 500px")
    private Integer qrSize = 150;

    @Min(value = 1, message = "Минимум 1 колонка")
    @Max(value = 10, message = "Максимум 10 колонок")
    private Integer columns = 4;

    private boolean autoColumns = true;

    private boolean generateHtml = false;

    public Integer getStart() { return start; }

    public void setStart(Integer start) { this.start = start; }

    public Integer getEnd() { return end; }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Integer getQrSize() { return qrSize; }
    public void setQrSize(Integer qrSize) { this.qrSize = qrSize; }

    public Integer getColumns() { return columns; }
    public void setColumns(Integer columns) { this.columns = columns; }

    public boolean isAutoColumns() { return autoColumns; }
    public void setAutoColumns(boolean autoColumns) { this.autoColumns = autoColumns; }

    public boolean isGenerateHtml() { return generateHtml; }
    public void setGenerateHtml(boolean generateHtml) { this.generateHtml = generateHtml; }

    public int getTotalNumbers() {
        return end - start + 1;
    }

    @Override
    public String toString() {
        return "QrGenerationRequest{" +
                "start=" + start +
                ", end=" + end +
                ", qrSize=" + qrSize +
                ", columns=" + columns +
                ", autoColumns=" + autoColumns +
                ", generateHtml=" + generateHtml +
                '}';
    }
}
