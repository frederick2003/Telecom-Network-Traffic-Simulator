package main.java.simulator;

public class Main {
    public static void main(String[] args) {
        // Create a simulation manager object.
        SimulationManager simulator = new SimulationManager();

        // Create an InputHandler Object to capture user defined data.
        InputHandler inputHandler = new InputHandler();

        // Create a SimulatorConfig object for storing the simulator data inputted by the user.
        SimulatorConfig simulatorConfig = inputHandler.getSimulatorConfiguration();

        // Add all sources to the simulator manager.
        for (int i = 0; i < simulatorConfig.numberSources; i++){
            simulator.addSource(new TrafficSource(i,
                                            simulatorConfig.onRate,
                                            simulatorConfig.alphaOn,
                                            simulatorConfig.xmOn,
                                            simulatorConfig.alphaOff,
                                            simulatorConfig.xmOff,
                                        1234L + i));

        }


        // Run simulator
        simulator.run(simulatorConfig.totalTime);

        // Outputs time-series Data
        simulator.getRecorder().outputTimeSeriesData();

        // Outputs Historical event logs
        simulator.getRecorder().printEventsLog();
    }
}