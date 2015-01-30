package nl.utwente.wsc.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import nl.utwente.wsc.models.WSc;

import org.joda.time.DateTime;

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
    
    public static void buildGraphReal(final Context context, GraphView graph, WSc wsc) { 
    	List<WSc> wscs = new ArrayList<WSc>();
    	wscs.add(wsc);
    	buildGraphReal(context, graph, wscs);
    }
    
    public static void buildGraphReal(final Context context, GraphView graph, List<WSc> wscs) {  
    	// first pile up the one day data from all WSc's, than split it in a number of data points
		TreeMap<DateTime, Double> tempHist = new TreeMap<DateTime, Double>();
    	DateTime now = DateTime.now();
    	for (WSc wsc : wscs) {
    		LinkedHashMap<DateTime, Double> histOfDay = wsc.getHistoryArrayOfDay(now);
    		if (histOfDay != null) {
    			tempHist.putAll(wsc.getHistoryArrayOfDay(now));
    		}
    	}
    	DataPoint[] history;
    	double min = 0;
    	double max = 100;
    	if (!tempHist.isEmpty()) {
        	DateTime firstDate = tempHist.firstKey();
        	DateTime lastDate = tempHist.lastKey();
    		history = new DataPoint[(int) (100 + 2 * Math.sqrt(tempHist.size()))];
        	int diffTime = (int) ((lastDate.getMillis() - firstDate.getMillis()) / history.length);
        	// get 'amountOfDataPoints' amount of data points between first and last date
        	for (WSc wsc : wscs) {
        		for (int i = 0; i < history.length - 1; i++) {
    				DateTime currDate = firstDate.plusMillis(diffTime * i);
        			if (history[i] == null) {
        				history[i] = new DataPoint(currDate.toDate(), 
        						wsc.getUsageOnCertainTime(currDate));
        			} else {
        				history[i] = new DataPoint(history[i].getX(), 
        						history[i].getY() + wsc.getUsageOnCertainTime(currDate));
        			}
        		}
    			if (history[history.length - 1] == null) {
    				history[history.length - 1] = new DataPoint(lastDate.toDate(), 
    						wsc.getUsageOnCertainTime(lastDate));
    			} else {
    				history[history.length - 1] = new DataPoint(history[history.length - 1].getX(), 
    						history[history.length - 1].getY() + wsc.getUsageOnCertainTime(lastDate));
    			}
        	}
        	min = Double.MAX_VALUE;
        	max = 0;
        	// find max and min values
        	for (DataPoint point : history) {
        		if (point.getY() < min) {
        			min = point.getY();
        		} else if (point.getY() > max) {
        			max = point.getY();
        		}
        	}
    	} else {
    		history = new DataPoint[2];
    		history[0] = new DataPoint(now.withTimeAtStartOfDay().toDate(), 0);
    		history[1] = new DataPoint(now.plusDays(1).withTimeAtStartOfDay().minusMillis(1).toDate(), 0);
    	}
    	// draw graph with data
    	LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(history);
		series.setDrawBackground(true);
		graph.removeAllSeries();
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
                    return super.formatLabel(value, isValueX) + " watt";
                }
            }
        });
        graph.getGridLabelRenderer().setNumHorizontalLabels(5);       
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(max + ((max - min) / 10));
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinX(history[0].getX());
        graph.getViewport().setMaxX(history[history.length-1].getX());
        graph.getViewport().setXAxisBoundsManual(true);   	
    }
    
    public static void buildGraphRandom(int min, int max, final Context context, GraphView graph, boolean turnedOn) {
    	DataPoint[] history = getRandomDataPoints(min, max);
    	if(!turnedOn) {
	    	for(int i = history.length-1; i > history.length-4; i--) {
	    		history[i] = new DataPoint(history[i].getX(), 0);
	    	}
    	}
    	LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(history);
		series.setDrawBackground(true);
		graph.removeAllSeries();
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
		for(int i = dps.length-1; i >= 0; i--) {
			calendar.add(Calendar.MINUTE, -5);
			double y = rand.nextInt((max - min) + 1) + min;
			dps[i] = new DataPoint(calendar.getTime(), y);
		}
		
		return dps;
    }
}
