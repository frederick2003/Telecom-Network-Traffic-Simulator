package simulator;

public class Main {
    public static void main(String[] args) {
        try{
            SimulationManager simulator = new SimulationManager();
            InputHandler inputHandler = new InputHandler();

            SimulatorConfig simulatorConfig = inputHandler.getSimulatorConfiguration();

            for (int i = 0; i < simulatorConfig.getNumberSources(); i++){
                simulator.addSource(new TrafficSource(i,
                        simulatorConfig.getOnRate(),
                        simulatorConfig.getAlphaOn(),
                        simulatorConfig.getXmOn(),
                        simulatorConfig.getAlphaOff(),
                        simulatorConfig.getXmOff(),
                        1234L + i,
                        simulatorConfig.getModelType(),
                        simulatorConfig.getHurst()));
            }


            // Run simulator
            simulator.run(simulatorConfig.getTotalTime());

            // Outputs time-series Data
            simulator.getRecorder().logTimeSeriesDataToCsv("data/time-series-data.csv");
            simulator.getRecorder().logEventsToCsv("data/events-log.csv");
        } catch(Exception e){
            System.out.println("Simulation Terminated Unexpectedly: " + e.getMessage());
        }
    }
}