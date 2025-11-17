package main.java.simulator;

public class StatisticsManager {

    private int totalEvents;
    private double maxAggregateRate;
    private double peakTraffic;
    private double averageTraffic;

    public StatisticsManager() {
        this.totalEvents = 0;
        this.maxAggregateRate = 0.0;
        this.peakTraffic = 0.0;
        this.averageTraffic = 0.0;
    }


    public void updateSimulationStatistics(Event event, TrafficSource currentSource) {
        double elapsedTime = event.getTime();
        int source = event.getSourceId();
        EventType type = event.getType();
    }

    private void updateTotalEvents(){
        this.totalEvents += 1;
    }

    private void updateMaxAggregateRate(){

    }

    private void updatePeakTraffic(){

    }

    private void updateAverageRate(){

    }


    // Add getters if needed
    public int getTotalEvents() { return totalEvents; }
}
