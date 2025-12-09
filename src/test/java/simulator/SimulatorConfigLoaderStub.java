package simulator;

public class SimulatorConfigLoaderStub {
    public static boolean successMode = true;

    public static SimulatorConfig loadFromFile(String path) {
        if (!successMode) {
            throw new RuntimeException("File not found");
        }

        return new SimulatorConfig(
                1000, 5, 1.5, 1.4, 1, 1,
                1.0,
                TrafficModelType.PARETO,
                0.8
        );
    }
}
