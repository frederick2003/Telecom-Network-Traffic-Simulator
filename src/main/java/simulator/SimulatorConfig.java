package simulator;

public class SimulatorConfig {
    private final double totalTime, alphaOn, alphaOff, xmOn, xmOff, onRate;
    private final int numberSources;
    private final TrafficModelType modelType;
    private final double hurst;


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

    public double getAlphaOn() {
        return alphaOn;
    }

    public double getAlphaOff() {
        return alphaOff;
    }

    public double getXmOn() {
        return xmOn;
    }

    public double getXmOff() {
        return xmOff;
    }

    public double getTotalTime(){
        return totalTime;
    }

    public double getOnRate() {
        return onRate;
    }

    public int getNumberSources() {
        return numberSources;
    }

    public TrafficModelType getModelType(){
        return modelType;
    }

    public double getHurst(){
        return hurst;
    }
}
