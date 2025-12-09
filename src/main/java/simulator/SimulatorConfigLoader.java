package simulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 */
public class SimulatorConfigLoader {

    /**
     *
     * @param filepath
     * @return
     * @throws IOException
     */
    public static SimulatorConfig loadFromFile(String filepath) throws IOException {

        double totalTime = -1;
        int numberSources = -1;
        double alphaOn = -1;
        double alphaOff = -1;
        double xmOn = -1;
        double xmOff = -1;
        double onRate = 1;
        TrafficModelType modelType = null;
        double hurst = 0.8;

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || !line.contains("=")) continue;

                String[] parts = line.split("=");
                if (parts.length != 2) continue;

                String key = parts[0].trim();
                String value = parts[1].trim();

                switch (key) {
                    case "model":
                        modelType = parseModelType(value);
                        break;

                    case "totalTime":
                        totalTime = Double.parseDouble(value);
                        break;

                    case "numSources":
                        numberSources = Integer.parseInt(value);
                        break;

                    case "alphaOn":
                        alphaOn = Double.parseDouble(value);
                        break;

                    case "alphaOff":
                        alphaOff = Double.parseDouble(value);
                        break;

                    case "xmOn":
                        xmOn = Double.parseDouble(value);
                        break;

                    case "xmOff":
                        xmOff = Double.parseDouble(value);
                        break;

                    case "hurst":
                        hurst = Double.parseDouble(value);
                        break;

                    default:
                        System.out.println("Warning: Unknown parameter: " + key);
                }
            }
        }

        validate(totalTime, numberSources, alphaOn, alphaOff, xmOn, xmOff, modelType, hurst);
        logSuccessMessage(filepath,totalTime, numberSources, alphaOn, alphaOff, xmOn, xmOff, modelType, hurst);

        return new SimulatorConfig(
                totalTime,
                numberSources,
                alphaOn,
                alphaOff,
                xmOn,
                xmOff,
                onRate,
                modelType,
                hurst);
    }

    /**
     *
     * @param value
     * @return
     */
    private static TrafficModelType parseModelType(String value) {
        value = value.trim().toLowerCase();

        return switch (value) {
            case "pareto" -> TrafficModelType.PARETO;
            case "fgn", "fractional_gaussian_noise", "fractional" -> TrafficModelType.FRACTIONAL_GAUSSIAN_NOISE;
            default -> throw new IllegalArgumentException(
                    "Unknown model type: " + value + " (expected: pareto or fgn)"
            );
        };
    }


    /**
     *
     * @param filePath
     * @param totalTime
     * @param numberSources
     * @param alphaOn
     * @param alphaOff
     * @param xmOn
     * @param xmOff
     */
    private static void logSuccessMessage(String filePath,
                                          double totalTime,
                                          int numberSources,
                                          double alphaOn,
                                          double alphaOff,
                                          double xmOn,
                                          double xmOff,
                                          TrafficModelType modelType,
                                          double hurst){
        System.out.println("File "+filePath+" parsed correctly");
        System.out.println("Pareto Parameters are as follows:");
        System.out.println("model="+modelType);
        System.out.println("totaltime="+totalTime);
        System.out.println("numSources="+numberSources);
        System.out.println("alphaOn="+ alphaOn);
        System.out.println("xmOn="+xmOn);
        System.out.println("alphaOff="+alphaOff);
        System.out.println("xmOff="+xmOff);
        System.out.println("hurst="+hurst);
    }

    /**
     *
     * @param totalTime
     * @param numberSources
     * @param alphaOn
     * @param alphaOff
     * @param xmOn
     * @param xmOff
     */
    private static void validate(double totalTime,
                                 int numberSources,
                                 double alphaOn,
                                 double alphaOff,
                                 double xmOn,
                                 double xmOff,
                                 TrafficModelType modelType,
                                 double hurst) {

        if (totalTime <= 0) throw new IllegalArgumentException("Invalid totalTime (alter your parameter file)");
        if (numberSources < 1) throw new IllegalArgumentException("numSources must be >= 1 (alter your parameter file)");
        if (alphaOn <= 1) throw new IllegalArgumentException("alphaOn must be > 1 (alter your parameter file)");
        if (alphaOff <= 1) throw new IllegalArgumentException("alphaOff must be > 1 (alter your parameter file)");
        if (xmOn <= 0) throw new IllegalArgumentException("xmOn must be > 0 (alter your parameter file)");
        if (xmOff <= 0) throw new IllegalArgumentException("xmOff must be > 0 (alter your parameter file)");
        if (modelType == null) throw new IllegalArgumentException("Model type must be specified (pareto or fgn)");
        if (hurst <= 0.5 || hurst >= 1.0) throw new IllegalArgumentException("Model type must be specified (pareto or fgn)");
    }
}
