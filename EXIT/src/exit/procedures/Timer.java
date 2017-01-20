/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exit.procedures;

/**
 * Methods of this class are used for timing the EXIT calculation times.
 * @author jmpaon
 */
public class Timer {
    
    public enum TimeUnit {
        
        MS, S, MIN;
        
        public String toString() {
            switch(this) {
                case MS  : return "Milliseconds";
                case S   : return "Seconds";
                case MIN : return "Minutes";
                default  : throw new EnumConstantNotPresentException(this.getClass(), this.name());
            }
        }
        
    }
    
    private boolean running;
    private Long startTime;
    private Long stopTime;
    
    public Timer() {
        
    }
    
    public Timer(boolean startImmediately) {
        if(startImmediately) start();
    }
    
    public void start() {
        if(running) throw new IllegalStateException("Timer is running");
        if(startTime != null) throw new IllegalStateException("Timer already has start time");
        
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }
    
    public void stop() {
        if(!running) throw new IllegalStateException("Timer is not running");
        if(stopTime != null) throw new IllegalStateException("Timer already has stop time");
        
        this.stopTime = System.currentTimeMillis();
        this.running = false;
    }
    
    public String stop(TimeUnit unit) {
        stop();
        return time(unit);
    }
    
    public Time stopGet() {
        stop();
        return new Time(stopTime-startTime);
    }
    
    
    
    
    public String time(TimeUnit unit) {
        if(startTime == null) throw new IllegalStateException("Timer has not been started");
        if(running) throw new IllegalStateException("Timer is running");
        double time_ms = stopTime - startTime;
        switch (unit) {
            case MS : return String.valueOf(time_ms) + " " + unit.toString();
            case S  : return String.valueOf(time_ms/1000) + " " + unit.toString();
            case MIN: return String.valueOf(time_ms/1000/1000) + " " + unit.toString();
            default : return String.valueOf(time_ms) + " " + TimeUnit.MS.toString();
        }
    }
    
    public void reset() {
        this.running = false;
        this.startTime = null;
        this.stopTime = null;
    }
    
    private long convertToSec(long milliSec) {
        return milliSec / 1000 ;
    }
    
    public static class Time {

        long time_ms;
        
        private Time(long ms) {
            assert ms >= 0 : "Time cannot be smaller than 0";
            time_ms = ms;
        }
        
        public String value(TimeUnit unit) {
            double ms = time_ms;
            switch (unit) {
                case MS : return String.format("%.0f %s", ms,        unit.toString());
                case S  : return String.format("%.2f %s", ms/1000,   unit.toString());
                case MIN: return String.format("%.2f %s", ms/1000000,unit.toString());
                //case MS : return String.valueOf(ms) + " " + unit.toString();
                //case S  : return String.valueOf(ms/1000) + " " + unit.toString();
                //case MIN: return String.valueOf(ms/1000/1000) + " " + unit.toString();
                default : return String.valueOf(ms) + " " + TimeUnit.MS.toString();
            }
        }
        
        @Override
        public String toString() { return String.format("%s %s", value(TimeUnit.MS), TimeUnit.MS.toString());}
        
    }
    
}
