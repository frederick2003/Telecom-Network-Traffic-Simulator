package main.java.simulator;

import java.util.Scanner;

public class InputHandler {
    private final Scanner scanner = new Scanner(System.in);

    public SimulatorConfig getSimulatorConfiguration(){
        logWelcome();
        double simulatorTime = getDoubleInput("Enter Simulator Total Run time");
        int simulatorNumberSources = getIntInput("Enter Number of Sources");
        double simulatorParetoAlphaOn = getDoubleInput("Pareto alpha (ON)");
        double simulatorParetoXmOn = getDoubleInput("Pareto xm (ON)");
        double simulatorParetoAlphaOff = getDoubleInput("Pareto alpha (OFF)");
        double simulatorParetoXmOff = getDoubleInput("Pareto xm (OFF)");

        return new SimulatorConfig(simulatorTime, simulatorNumberSources, simulatorParetoAlphaOn, simulatorParetoAlphaOff, simulatorParetoXmOn, simulatorParetoXmOff, 1.0);
    }

    /**
     * Helper function to prompting the user to input simulator parameters.
     *
     * @param prompt the string prompt to output to the CLI
     * @return a double type for the particular input prompt.
     */
    private double getDoubleInput(String prompt){
        while(true){
            System.out.print(prompt);
            String input = scanner.nextLine();
            try{
                return Double.parseDouble(input);
            } catch (NumberFormatException e){
                System.out.println("Invalid number try again");
            }
        }
    }

    private int getIntInput(String prompt){
        return (int) getDoubleInput(prompt);
    }

    private void logWelcome(){
        System.out.println("--- Telecom Network Traffic Simulator ---");
    }
}
