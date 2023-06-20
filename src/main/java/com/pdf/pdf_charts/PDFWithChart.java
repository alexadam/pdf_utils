package com.pdf.pdf_charts;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeries;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PDFWithChart {

    public static void main(String[] args) {
        // generate dummy data stream
        TimeSeries dummyData = DummyDataGenerator.generate("Price", 1000, 50);
        TimeSeries[] series = {dummyData};
        Color[] colors = {Color.decode("#000000")};

        // generate chart
        var chartData = ChartGenerator.fromTimeSeries(series, "Random Data", "Date", "Price", colors);

        // save as jpg
        ChartGenerator.saveAsJPG(chartData, "chart.jpg", 400, 300);

        // generate pdf
        generatePDF("chart.pdf", chartData);
    }

    public static void generatePDF(String filePath, JFreeChart chart) {
        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);

            // add graph
            BufferedImage chartImage = chart.createBufferedImage(500, 400);
            PDImageXObject pdfChartImage = JPEGFactory.createFromImage(document, chartImage, 1f);
            content.drawImage(pdfChartImage, 20, 300);

            // Doc. Title
            content.beginText();
            content.setFont(PDType1Font.TIMES_BOLD, 24);
            content.newLineAtOffset(50, 750);
            content.showText("Random Data Graph");
            content.endText();

            // save
            content.close();
            document.save(filePath);
            document.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
