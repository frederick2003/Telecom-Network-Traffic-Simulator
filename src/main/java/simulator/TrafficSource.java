package main.java.simulator;

import java.util.Random;

public class TrafficSource {
    private final int id;
    private boolean isOn;
    private final double onRate;
    private final double alphaOn, xmOn;
    private final double alphaOff, xmOff;
    private final Random rng;

    public TrafficSource(int id, double onRate,
                         double alphaOn, double xmOn,
                         double alphaOff, double xmOff,
                         long seed) {
        this.id = id;
        this.onRate = onRate;
        this.alphaOn = alphaOn;
        this.xmOn = xmOn;
        this.alphaOff = alphaOff;
        this.xmOff = xmOff;
        this.rng = new Random(seed);
        this.isOn = false;
    }

    public int getId() { return id; }
    public boolean getIsOn() { return isOn; }
    public double getOnRate() { return onRate; }

    public Event scheduleInitialEvent(double now) {
        // Start OFF -> schedule first ON
        double dt = sampleOffDuration();
        return new Event(now + dt, id, EventType.SOURCE_ON);
    }

    public Event scheduleNextEvent(double now) {
        // Toggle state and schedule opposite
        isOn = !isOn;
        double dt = isOn ? sampleOnDuration() : sampleOffDuration();
        return new Event(now + dt,  id, isOn ? EventType.SOURCE_OFF : EventType.SOURCE_ON);
    }

    public double sampleOnDuration() {
        return ParetoSampler.sample(alphaOn, xmOn, rng);
    }

    public double sampleOffDuration() {
        return ParetoSampler.sample(alphaOff, xmOff, rng);
    }
}
