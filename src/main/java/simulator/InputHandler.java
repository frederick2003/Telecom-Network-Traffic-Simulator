package main.java.simulator;

import java.util.Scanner;

/**
 * Handles all console-based input for the Telecom Network Traffic Simulator.
 *
 * This class is responsible for prompting the user for simulation parameters,
 * validating the input, and constructing a {@link SimulatorConfig} object.
 *
 * This class deals makes sure all input is in a valid format and deals with error handling.
 */
public class InputHandler {
    /** Scanner instance for reading console input. */
    private final Scanner scanner = new Scanner(System.in);
    private double hurst = 0.8;

    /**
     * Prompts the user for all required simulator parameters, validates the input,
     * and constructs a fully populated SimulatorConfig.
     *
     * @return a SimulatorConfig object containing all simulation parameters.
     */
    public SimulatorConfig getSimulatorConfiguration() {
        logWelcome();
        while(true) {
            String parameterConfigChoice = getSimulatorConfigChoice();
            checkQuit(parameterConfigChoice);

            switch (parameterConfigChoice) {
                case "manual":
                    System.out.println("Manual Parameter Input chosen");
                    return getManualSimulationConfig();
                case "load":
                    System.out.println("Load Parameter chosen");
                    SimulatorConfig loadedConfig = loadConfigFromFile();
                    if (loadedConfig != null){
                        return loadedConfig;
                    }
                    System.out.println("File cannot be loaded");
                    System.out.println("Check file is in the correct directory");
                    System.out.println("File must be a .txt file in the following format:");
                    System.out.println("totalTime=1000\n" +
                            "numSources=20\n" +
                            "alphaOn=1.5\n" +
                            "xmOn=1\n" +
                            "alphaOff=1.4\n" +
                            "xmOff=1");
                default:
                    System.out.println("Invalid option. Please type 'manual', 'load', or 'quit'.");
            }
        }
    }

    private SimulatorConfig getManualSimulationConfig(){

        int choice = getPositiveInt("\nChoose Traffic Model:\n" +
                "1 = Pareto (recommended)\n" +
                "2 = Fractional Gaussian Noise\n" +
                ":");

        TrafficModelType modelType = switch (choice){
            case 1 -> TrafficModelType.PARETO;
            case 2 -> TrafficModelType.FRACTIONAL_GAUSSIAN_NOISE;
            default -> TrafficModelType.PARETO;
        };

        if (modelType == TrafficModelType.FRACTIONAL_GAUSSIAN_NOISE){
            hurst = getPositiveDouble("Enter Hurst parameter H (0.5 < H < 1.0, typical: 0.7–0.95):");
        }
        if (hurst<=0.5 || hurst >=0.95){
            System.out.println("Invalid H. Setting default H = 0.8");
            hurst = 0.8;
        }

        double simulatorTime = getPositiveDouble(
                "Enter Simulator Total Run Time (e.g. 1000): "
        );

        int simulatorNumberSources = getPositiveInt(
                "Enter Number of Sources (must be at least 1): "
        );

        double simulatorParetoAlphaOn = getPositiveDouble(
                "Pareto alpha (ON) > 1 recommended: "
        );

        double simulatorParetoXmOn = getPositiveDouble(
                "Pareto xm (ON) > 0: "
        );

        double simulatorParetoAlphaOff = getPositiveDouble(
                "Pareto alpha (OFF) > 1 recommended: "
        );

        double simulatorParetoXmOff = getPositiveDouble(
                "Pareto xm (OFF) > 0: "
        );

        return new SimulatorConfig(
                simulatorTime,
                simulatorNumberSources,
                simulatorParetoAlphaOn,
                simulatorParetoAlphaOff,
                simulatorParetoXmOn,
                simulatorParetoXmOff,
                1.0,
                modelType,
                hurst
        );
    }

    /**
     * Prompts the user for a strictly positive double value.
     *
     * This method ensures: Input is not empty, input is a valid numeric type and
     * value is strictly greater than zero.
     *
     * @param prompt: the message to display when requesting input
     * @return a valid, positive double value entered by the user
     */
    private double getPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            checkQuit(input);

            if (input.isEmpty()) {
                System.out.println("Input cannot be empty.");
                continue;
            }

            try {
                double value = Double.parseDouble(input);
                if (value <= 0) {
                    System.out.println("Value must be greater than zero.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid numeric value.");
            }
        }
    }

    /**
     * Prompts the user for a strictly positive integer.
     *
     * This method uses getPositiveDouble(String) to parse input,
     * then enforces integer-only behaviour by checking that the input has
     * no fractional component.
     *
     * @param prompt  the message to display when requesting input
     * @return a valid positive integer entered by the user
     */
    private int getPositiveInt(String prompt) {
        while (true) {
            double val = getPositiveDouble(prompt);

            // enforce integer-only behaviour
            if (val % 1 != 0) {
                System.out.println("Value must be a whole number (integer).");
                continue;
            }

            return (int) val;
        }
    }

    /**
     *
     * @return
     */
    public SimulatorConfig loadConfigFromFile(){
        System.out.print("Enter configuration file name: ");
        String filepath = scanner.next();

        try {
            SimulatorConfig config = SimulatorConfigLoader.loadFromFile(filepath);
            System.out.println("Configuration loaded successfully from " + filepath);
            return config;
        } catch (Exception e) {
            System.out.println("Error loading configuration: " + e.getMessage());
            return null; // caller decides what to do next
        }
    }

    /**
     * Checks whether the input is a quit command.
     * If so, exits the program gracefully.
     */
    private void checkQuit(String input) {
        if (input.equalsIgnoreCase("q") || input.equalsIgnoreCase("quit")) {
            System.out.println("User Quit Simulation!");
            System.exit(0);
        }
    }

    /**
     *
     * @return
     */
    private String getSimulatorConfigChoice(){
        System.out.println("\nChoose input mode:");
        System.out.println("manual   - Enter parameters manually");
        System.out.println("load     - Load parameters from a file");
        System.out.println("quit     - Exit program");
        System.out.print("Enter choice (manual, load or quit): ");
        String choice = scanner.nextLine().trim().toLowerCase();
        return choice;
    }

    /**
     * Displays a welcome message for the simulator.
     * Called automatically when configuration begins.
     */
    private void logWelcome() {
        System.out.println("--- Telecom Network Traffic Simulator ---");
        System.out.println("Please enter all simulation parameters below.");
        System.out.println("You can type 'q' or 'quit' at any time to exit.");
    }
}
