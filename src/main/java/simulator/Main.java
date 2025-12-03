package main.java.simulator;

public class Main {
    public static void main(String[] args) {
        try{
            SimulationManager simulator = new SimulationManager();
            InputHandler inputHandler = new InputHandler();

            SimulatorConfig simulatorConfig = inputHandler.getSimulatorConfiguration();

            for (int i = 0; i < simulatorConfig.numberSources; i++){
                simulator.addSource(new TrafficSource(i,
                        simulatorConfig.onRate,
                        simulatorConfig.alphaOn,
                        simulatorConfig.xmOn,
                        simulatorConfig.alphaOff,
                        simulatorConfig.xmOff,
                        1234L + i,
                        simulatorConfig.modelType,
                        simulatorConfig.hurst));
            }


            // Run simulator
            simulator.run(simulatorConfig.totalTime);

            // Outputs time-series Data
            simulator.getRecorder().logTimeSeriesDataToCsv("data/time-series-data.csv");
            simulator.getRecorder().logEventsToCsv("data/events-log.csv");
        } catch(Exception e){
            System.out.println("Simulation Terminated Unexpectedly: " + e.getMessage());
        }
    }
}