package com.pdf.pdf_charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ChartGenerator {

    public static JFreeChart fromTimeSeries(TimeSeries[] timeSeries, String title, String XLabel, String YLabel, Color[] seriesColors) {
        TimeSeriesCollection tsCollection = new TimeSeriesCollection();
        for (int i = 0; i < timeSeries.length; i++) {
            tsCollection.addSeries(timeSeries[i]);
        }

        final XYDataset dataset = (XYDataset) tsCollection;
        JFreeChart timeChart = ChartFactory.createTimeSeriesChart(
                title,
                XLabel,
                YLabel,
                dataset,
                true,
                false,
                false);

        XYPlot plot = timeChart.getXYPlot();
        plot.setBackgroundPaint(Color.decode("#F1F0EA"));

        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);
        plot.setRangeGridlineStroke(new BasicStroke(0.1f));
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.BLACK);
        plot.setDomainGridlineStroke(new BasicStroke(0.1f));

        plot.setDomainZeroBaselineVisible(false);
        plot.setOutlineVisible(false);
        plot.setRangeZeroBaselineVisible(false);
        plot.setDomainCrosshairVisible(false);

        XYItemRenderer renderer = plot.getRenderer();
        for (int i = 0; i < timeSeries.length; i++) {
            renderer.setSeriesPaint(i, seriesColors[i]);
        }
        renderer.setDefaultStroke(new BasicStroke(1.0f));
        renderer.setDefaultOutlineStroke(new BasicStroke(0.5f));
        ((AbstractRenderer) renderer).setAutoPopulateSeriesStroke(false);

        return timeChart;
    }

    public static void saveAsJPG(JFreeChart chart, String filePath, int width, int height) {
        File timeChart = new File(filePath);
        try {
            ChartUtils.saveChartAsJPEG(timeChart, chart, width, height);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
