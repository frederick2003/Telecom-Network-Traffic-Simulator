package main.java.simulator;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
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
                                        1234L + i));

        }
        simulator.run(simulatorConfig.totalTime);
        System.out.println("Points recorded: " + simulator.getRecorder().asPoints().size());
        System.out.println("Done at t=" + simulator.getNow());

    }
}