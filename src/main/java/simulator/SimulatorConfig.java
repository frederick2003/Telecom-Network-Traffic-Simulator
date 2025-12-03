package main.java.simulator;

public class SimulatorConfig {
    public final double totalTime, alphaOn, alphaOff, xmOn, xmOff, onRate;
    public final int numberSources;
    public final TrafficModelType modelType;
    public final double hurst;


    public SimulatorConfig(double totalTime,
                           int numberSources,
                           double alphaOn,
                           double alphaOff,
                           double xmOn,
                           double xmOff,
                           double onRate,
                           TrafficModelType modelType,
                           double hurst
                           ){
        this.totalTime = totalTime;
        this.numberSources = numberSources;
        this.alphaOn = alphaOn;
        this.alphaOff = alphaOff;
        this.xmOn = xmOn;
        this.xmOff = xmOff;
        this.onRate = onRate;
        this.modelType = modelType;
        this.hurst=hurst;
    }
}
