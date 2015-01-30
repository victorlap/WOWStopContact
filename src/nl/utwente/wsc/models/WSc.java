package nl.utwente.wsc.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.joda.time.DateTime;

import com.jjoe64.graphview.series.DataPoint;

public class WSc implements Serializable {

	private static final long serialVersionUID = -8500471167788498880L;
	private static final double MEASURE_INTERVAL = 0.0005; //amount of Kwh per measurement.

	private String name;
	private String hostname;
	private int port;
	
	private boolean connected;
	private boolean turnedOn;
	private boolean busy;
	private ColorType color;
	private LinkedHashMap<DateTime, Double> history;
	
	public WSc(String name, String hostname, int port) {
		this.name = name;
		this.hostname = hostname;
		this.port = port;
		this.color = ColorType.NONE;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setTurnedOn(boolean turnedOn) {
		this.turnedOn = turnedOn;
	}
	
	public boolean isTurnedOn() {
		return turnedOn;
	}
	
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public void setBusy(boolean busy) {
		this.busy = busy;
	}
	
	public boolean isBusy() {
		return busy;
	}
	
	public void setColor(ColorType color) {
		this.color = color;
	}
	
	public ColorType getColor() {
		return color == null ? ColorType.NONE : color;
	}
	
	public boolean hasHistory() {
		return history != null;
	}
	
	public void addHistory(DateTime time, double value) {		
		if (!hasHistory()) {
			this.history = new LinkedHashMap<DateTime, Double>();
		}
		this.history.put(time, value);
	}
	
	public void addHistory(LinkedHashMap<DateTime, Double> history) {		
		if (!hasHistory()) {
			this.history = new LinkedHashMap<DateTime, Double>();
		}
		this.history.putAll(history);
	}
	
	public void convertAndSetHistory(LinkedHashMap<Long, Double> historyRaw) {
		LinkedHashMap<DateTime, Double> converted = new LinkedHashMap<DateTime, Double>();
		for (long key : historyRaw.keySet()) {
			converted.put(new DateTime(key), historyRaw.get(key));
		}
		setHistory(converted);
	}
	
	public void convertAndAddHistory(LinkedHashMap<Long, Double> historyRaw) {
		LinkedHashMap<DateTime, Double> converted = new LinkedHashMap<DateTime, Double>();
		for (long key : historyRaw.keySet()) {
			converted.put(new DateTime(key), historyRaw.get(key));
		}
		addHistory(converted);
	}
	
	public void setHistory(LinkedHashMap<DateTime, Double> history) {
		this.history = history;
	}
	
	public LinkedHashMap<DateTime, Double> getHistoryArray() {
		return history;
	}
	
	public LinkedHashMap<DateTime, Double> getHistoryArrayOfDay(DateTime day) {
		if (!hasHistory()) {
			return null;
		}
		LinkedHashMap<DateTime, Double> dayHistory = new LinkedHashMap<DateTime, Double>();
		for (DateTime dt : history.keySet()) {
			// check same day of same year
			if (dt.getYear() == day.getYear() && dt.getDayOfYear() == day.getDayOfYear()) {
				dayHistory.put(dt, history.get(dt));
			}
		}
		return dayHistory;
	}
	
	public double getUsageOnCertainTime(DateTime time) {
		if (!hasHistory()) {
			return 0;
		} 		
		boolean isFirst = true;
		for (DateTime date : history.keySet()) {
			if (date.isEqual(time)) {
				return history.get(date);
			}
			if (date.isAfter(time)) {
				if (isFirst) {
					// we don't know the usage before the first value
					return 0; 
				} else {
					return history.get(date);
				}				 
			}
			isFirst = false;
		}
		// our history does not go that far
		return 0; 
	}
	
	public Entry<DateTime, Double> getLatestSample() {
		if (!hasHistory()) {
			return null;
		} 
		Entry<DateTime, Double> latestSample = null;
		for (Entry<DateTime, Double> entry : history.entrySet()) {
			latestSample = entry;
		}
		return latestSample;
	}
	
	/**
	 * The time the socket is powered on (time since the last power on).
	 * 
	 * @return the time in ms
	 */
	public double getPowerOnTime() {
		double totalDuration = 0; // in ms
		DateTime lastTime = null;
		for (DateTime date : history.keySet()) {
			if (lastTime != null) {
				if (history.get(date) == 0) {
					totalDuration = 0;
				} else {
					totalDuration += date.getMillis() - lastTime.getMillis();
				}
			} 
			lastTime = date;
		}
		return totalDuration;
	}
	
	/**
	 * The current power draw in watt.
	 * @return
	 */
	public double getCurrentPowerDraw() {
		return hasHistory() ? getLatestSample().getValue() : 0d;
	}
	
	/**
	 * The last sample time in milliseconds (will be 0 when there is
	 * no history).
	 * @return
	 */
	public long getLastSampleTime() {
		return hasHistory() ? getLatestSample().getKey().getMillis() : 0;
	}
	
	/**
	 * The daily usage in Kilowatt/hour (up to now).
	 * @return the daily usage
	 */
	public double getDailyUsage(DateTime day) {
		if (!hasHistory()) {
			return 0d;
		} 
		double usage = 0;
		for (DateTime date : history.keySet()) {	
			// if sample taken on same day
			if (date.getYear() == day.getYear() && 
					date.getDayOfYear() == day.getDayOfYear()) {
				// non-zero measure
				if (history.get(date) > 0) {
					usage += MEASURE_INTERVAL;
				}
			}
		}
		return usage;
	}
	/**
	 * The estimated yearly usage in Kilowatt/hour.<br>
	 * If less than 365 days of data are present, the usage is 
	 * extrapolated.
	 * 
	 * @return the yearly estimate
	 */	
	public double getYearlyEstimate(DateTime year) {
		if (!hasHistory()) {
			return 0d;
		} 
		double usage = 0;
		int lastDay = 0;
		int numOfDays = 0;
		for (DateTime date : history.keySet()) {	
			// if sample taken on same day
			if (date.getYear() == year.getYear()) {
				if (date.getDayOfYear() > lastDay) {
					lastDay = date.getDayOfYear();
					numOfDays++;
				}
				// non-zero measure
				if (history.get(date) > 0) {
					usage += MEASURE_INTERVAL;
				}
			}
		}
		return usage / numOfDays * 365;
		
	}

	public DataPoint[] getFakeHistory() {
		DataPoint[] dps = new DataPoint[50];
		Random rand = new Random();
		 Calendar calendar = Calendar.getInstance();
		for(int i = 0; i < dps.length; i++) {
			calendar.add(Calendar.DATE, 1);
			double y = rand.nextInt((50 - 10) + 1) + 10;
			dps[i] = new DataPoint(calendar.getTime(), y);
		}
		return dps;
	}
	
	public int getHistoryLength() {
		return history.size();
	}
	
	@Override
	public String toString() {
		return getName() + " " + getHostname() + ":" + getPort();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == this) {
			return true;
		}
		if(other instanceof WSc) {
			WSc w = (WSc) other;
			if(getName() != w.getName()) return false; 
			if(getHostname() != w.getHostname()) return false;
			if(getPort() != w.getPort()) return false;
			return true;
		}
		return false;
	}

}
