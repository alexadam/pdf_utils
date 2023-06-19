package com.pdf.pdf_charts;

import org.jfree.data.time.TimeSeries;

import java.awt.*;

public class PDFWithChart {

    public static void main(String[] args) {
        TimeSeries dummyData = DummyDataGenerator.generate("Price", 1000, 50);
        TimeSeries[] series = {dummyData};

        Color[] colors = {Color.decode("#000000")};

        var chartData = ChartGenerator.fromTimeSeries(series, "Random Data", "Price", "Date", colors);

        ChartGenerator.saveAsJPG(chartData, "chart.jpg", 800, 600);
    }

}
