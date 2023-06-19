package com.pdf.pdf_charts;

import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DummyDataGenerator {

    public static TimeSeries generate(String name, int nrOfElements, double startValue) {
        final TimeSeries series = new TimeSeries( name );
        LocalDateTime dtl = LocalDateTime.now();
        double value = startValue;

        for (int i = 0; i < nrOfElements; i++) {
            var ld = dtl.minus(nrOfElements - i, ChronoUnit.DAYS);
            var current = new Day(ld.getDayOfMonth(), ld.getMonthValue(), ld.getYear());

            try {
                value = value + Math.random( ) - 0.5;
                series.add( current , Double.valueOf(value) );
            } catch ( SeriesException e ) {
                System.err.println( "Error adding to series" );
            }
        }

        return series;
    }

}
