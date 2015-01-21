package nl.utwente.wsc.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import nl.utwente.wsc.models.WSc;
import android.content.Context;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * Tools holding class.
 *
 * @author rvemous
 */
public class Tools {
	
	public static WSc updated;
	public static WSc removed;
    
    /**
     * Waits for the specified time in milliseconds.<br>
     * It can be interrupted.
     * 
     * @param sleepTime time to sleep
     */
    public static void waitForMs(long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {}
    }

    /**
     * Waits for the specified time in nanoseconds.<br>
     * It cannot be interrupted.
     * 
     * @param sleepTime time to sleep
     */	
    public static void waitForNs(long sleepTime) {
        long currTime = System.nanoTime();
        while (System.nanoTime() - currTime <= sleepTime);
    }
    
    public static void buildGraph(int min, int max, final Context context, GraphView graph, boolean turnedOn) {
    	DataPoint[] history = getRandomDataPoints(min, max);
    	if(!turnedOn) {
	    	for(int i = history.length-1; i > history.length-4; i--) {
	    		history[i] = new DataPoint(history[i].getX(), 0);
	    	}
    	}
    	LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(history);
		series.setDrawBackground(true);
		graph.addSeries(series);

		// set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
        	@Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    return new DateAsXAxisLabelFormatter(context, new SimpleDateFormat("HH:mm")).formatLabel(value, isValueX);
                } else {
                    // show currency for y values
                    return super.formatLabel(value, isValueX) + " W";
                }
            }
        });
        graph.getGridLabelRenderer().setNumHorizontalLabels(5); // only 4 because of the space

        // set manual x bounds to have nice steps
        //graph.getViewport().setScalable(true);
        //graph.getViewport().setScrollable(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(max+10);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinX(history[0].getX());
        graph.getViewport().setMaxX(history[history.length-1].getX());
        graph.getViewport().setXAxisBoundsManual(true);
    }
    
    private static DataPoint[] getRandomDataPoints(int min, int max) {
    	DataPoint[] dps = new DataPoint[50];
		Random rand = new Random();
		 Calendar calendar = Calendar.getInstance();
		for(int i = 0; i < dps.length; i++) {
			calendar.add(Calendar.MINUTE, 5);
			double y = rand.nextInt((max - min) + 1) + min;
			dps[i] = new DataPoint(calendar.getTime(), y);
		}
		
		return dps;
    }
}
