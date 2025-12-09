package simulator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SimulatorConfigLoaderTest {

    @TempDir
    Path tempDir;

    // Utility method to write a temporary config file
    private Path writeConfig(String content) throws IOException {
        Path file = tempDir.resolve("config.txt");
        Files.writeString(file, content);
        return file;
    }

    @Test
    void testValidConfigLoadsCorrectly() throws IOException {
        String config = """
                model=pareto
                totalTime=1000
                numSources=5
                alphaOn=1.5
                alphaOff=1.8
                xmOn=2.0
                xmOff=3.0
                hurst=0.8
                """;

        Path file = writeConfig(config);

        SimulatorConfig cfg = SimulatorConfigLoader.loadFromFile(file.toString());

        assertEquals(1000, cfg.getTotalTime());
        assertEquals(5, cfg.getNumberSources());
        assertEquals(1.5, cfg.getAlphaOn());
        assertEquals(1.8, cfg.getAlphaOff());
        assertEquals(2.0, cfg.getXmOn());
        assertEquals(3.0, cfg.getXmOff());
        assertEquals(0.8, cfg.getHurst());
        assertEquals(TrafficModelType.PARETO, cfg.getModelType());
    }

    @Test
    void testInvalidModelTypeThrows() throws IOException {
        String config = """
                model=unknownModel
                totalTime=100
                numSources=2
                alphaOn=1.5
                alphaOff=1.5
                xmOn=1
                xmOff=1
                hurst=0.8
                """;

        Path file = writeConfig(config);

        assertThrows(IllegalArgumentException.class,
                () -> SimulatorConfigLoader.loadFromFile(file.toString()));
    }

    @Test
    void testMissingTotalTimeThrows() throws IOException {
        String config = """
                model=pareto
                numSources=2
                alphaOn=1.5
                alphaOff=1.5
                xmOn=1
                xmOff=1
                hurst=0.8
                """;

        Path file = writeConfig(config);

        assertThrows(IllegalArgumentException.class,
                () -> SimulatorConfigLoader.loadFromFile(file.toString()));
    }

    @Test
    void testInvalidHurstThrows() throws IOException {
        String config = """
                model=pareto
                totalTime=100
                numSources=2
                alphaOn=1.5
                alphaOff=1.5
                xmOn=1
                xmOff=1
                hurst=0.2
                """; // hurst must be >0.5 and <1.0

        Path file = writeConfig(config);

        assertThrows(IllegalArgumentException.class,
                () -> SimulatorConfigLoader.loadFromFile(file.toString()));
    }

    @Test
    void testNegativeAlphaOnThrows() throws IOException {
        String config = """
                model=pareto
                totalTime=100
                numSources=2
                alphaOn=0.5
                alphaOff=1.5
                xmOn=1
                xmOff=1
                hurst=0.8
                """;

        Path file = writeConfig(config);

        assertThrows(IllegalArgumentException.class,
                () -> SimulatorConfigLoader.loadFromFile(file.toString()));
    }

    @Test
    void testZeroXmOnThrows() throws IOException {
        String config = """
                model=pareto
                totalTime=100
                numSources=2
                alphaOn=1.5
                alphaOff=1.5
                xmOn=0
                xmOff=1
                hurst=0.8
                """;

        Path file = writeConfig(config);

        assertThrows(IllegalArgumentException.class,
                () -> SimulatorConfigLoader.loadFromFile(file.toString()));
    }

    @Test
    void testUnknownParameter() throws IOException{
        String config = """
                model=pareto
                totalTime=100
                numSources=2
                alphaOn=1.5
                alphaOff=1.5
                xmOn=0
                xmOff=1
                hurst=0.8
                unknownParameter=1.0
                """;

        Path file = writeConfig(config);
        assertThrows(IllegalArgumentException.class,
                () -> SimulatorConfigLoader.loadFromFile(file.toString()));
    }

    @Test
    void testParameterFileWithWhiteSpace() throws IOException{
        String config = """
                model=pareto
                totalTime=100
                numSources=2
                
                alphaOn=1.5
                alphaOff=1.5
                
                xmOn=2.0
                xmOff=1
                
                hurst=0.8
                """;

        Path file = writeConfig(config);

        SimulatorConfig cfg = SimulatorConfigLoader.loadFromFile(file.toString());

        assertEquals(100, cfg.getTotalTime());
        assertEquals(2, cfg.getNumberSources());
        assertEquals(1.5, cfg.getAlphaOn());
        assertEquals(1.5, cfg.getAlphaOff());
        assertEquals(2.0, cfg.getXmOn());
        assertEquals(1.0, cfg.getXmOff());
        assertEquals(0.8, cfg.getHurst());
        assertEquals(TrafficModelType.PARETO, cfg.getModelType());
    }

    @Test
    void testMultipleParametersPerLine() throws IOException{
        String config = """
                model=pareto
                totalTime=100
                numSources=2
                
                random line in the middle for some reason?
                
                alphaOn=1.5
                alphaOff=1.5
                
                xmOn=2.0
                xmOff=1
                
                hurst=0.8
                """;

        Path file = writeConfig(config);

        SimulatorConfig cfg = SimulatorConfigLoader.loadFromFile(file.toString());

        assertEquals(100, cfg.getTotalTime());
        assertEquals(2, cfg.getNumberSources());
        assertEquals(1.5, cfg.getAlphaOn());
        assertEquals(1.5, cfg.getAlphaOff());
        assertEquals(2.0, cfg.getXmOn());
        assertEquals(1.0, cfg.getXmOff());
        assertEquals(0.8, cfg.getHurst());
        assertEquals(TrafficModelType.PARETO, cfg.getModelType());
    }

    @Test
    void testInvalidLineSyntax() throws IOException{
        String config = """
                model=pareto
                totalTime=100
                numSources=2
                
                randomParameter=extra=9.0
                
                alphaOn=1.5
                alphaOff=1.5
                
                xmOn=2.0
                xmOff=1
                
                hurst=0.8
                """;

        Path file = writeConfig(config);

        SimulatorConfig cfg = SimulatorConfigLoader.loadFromFile(file.toString());

        assertEquals(100, cfg.getTotalTime());
        assertEquals(2, cfg.getNumberSources());
        assertEquals(1.5, cfg.getAlphaOn());
        assertEquals(1.5, cfg.getAlphaOff());
        assertEquals(2.0, cfg.getXmOn());
        assertEquals(1.0, cfg.getXmOff());
        assertEquals(0.8, cfg.getHurst());
        assertEquals(TrafficModelType.PARETO, cfg.getModelType());
    }

    @Test
    void testFractionalGaussianNoiseModel() throws IOException{
        String config = """
                model=fgn
                totalTime=100
                numSources=2
                
                randomParameter=extra=9.0
                
                alphaOn=1.5
                alphaOff=1.5
                
                xmOn=2.0
                xmOff=1
                
                hurst=0.8
                """;
        Path file = writeConfig(config);

        SimulatorConfig cfg = SimulatorConfigLoader.loadFromFile(file.toString());

        assertEquals(100, cfg.getTotalTime());
        assertEquals(2, cfg.getNumberSources());
        assertEquals(1.5, cfg.getAlphaOn());
        assertEquals(1.5, cfg.getAlphaOff());
        assertEquals(2.0, cfg.getXmOn());
        assertEquals(1.0, cfg.getXmOff());
        assertEquals(0.8, cfg.getHurst());
        assertEquals(TrafficModelType.FRACTIONAL_GAUSSIAN_NOISE, cfg.getModelType());
    }

    @Test
    void testFractionalGaussianNoiseModel2() throws IOException{
        String config = """
                model=fractional_gaussian_noise
                totalTime=100
                numSources=2
                
                randomParameter=extra=9.0
                
                alphaOn=1.5
                alphaOff=1.5
                
                xmOn=2.0
                xmOff=1
                
                hurst=0.8
                """;
        Path file = writeConfig(config);

        SimulatorConfig cfg = SimulatorConfigLoader.loadFromFile(file.toString());

        assertEquals(100, cfg.getTotalTime());
        assertEquals(2, cfg.getNumberSources());
        assertEquals(1.5, cfg.getAlphaOn());
        assertEquals(1.5, cfg.getAlphaOff());
        assertEquals(2.0, cfg.getXmOn());
        assertEquals(1.0, cfg.getXmOff());
        assertEquals(0.8, cfg.getHurst());
        assertEquals(TrafficModelType.FRACTIONAL_GAUSSIAN_NOISE, cfg.getModelType());
    }

    @Test
    void testFractionalGaussianNoiseModel3() throws IOException{
        String config = """
                model=fractional
                totalTime=100
                numSources=2
                
                randomParameter=extra=9.0
                
                alphaOn=1.5
                alphaOff=1.5
                
                xmOn=2.0
                xmOff=1
                
                hurst=0.8
                """;
        Path file = writeConfig(config);

        SimulatorConfig cfg = SimulatorConfigLoader.loadFromFile(file.toString());

        assertEquals(100, cfg.getTotalTime());
        assertEquals(2, cfg.getNumberSources());
        assertEquals(1.5, cfg.getAlphaOn());
        assertEquals(1.5, cfg.getAlphaOff());
        assertEquals(2.0, cfg.getXmOn());
        assertEquals(1.0, cfg.getXmOff());
        assertEquals(0.8, cfg.getHurst());
        assertEquals(TrafficModelType.FRACTIONAL_GAUSSIAN_NOISE, cfg.getModelType());
    }

    @Test
    void testZeroNumberOfSources() throws IOException{
        String config = """
                model=pareto
                totalTime=100
                numSources=0
                alphaOn=1.5
                alphaOff=1.5
                xmOn=1.0
                xmOff=1
                hurst=0.8
                """;
        Path file = writeConfig(config);
        assertThrows(IllegalArgumentException.class,
                () -> SimulatorConfigLoader.loadFromFile(file.toString()));
    }

    @Test
    void testAlphaLessThanOne() throws IOException{
        String config = """
                model=pareto
                totalTime=100
                numSources=2
                alphaOn=1.5
                alphaOff=0.8
                xmOn=1.0
                xmOff=1
                hurst=0.8
                """;
        Path file = writeConfig(config);
        assertThrows(IllegalArgumentException.class,
                () -> SimulatorConfigLoader.loadFromFile(file.toString()));
    }

    @Test
    void testXmLessThanOne() throws IOException{
        String config = """
                model=pareto
                totalTime=100
                numSources=2
                alphaOn=1.5
                alphaOff=1.8
                xmOn=1.0
                xmOff=0
                hurst=0.8
                """;
        Path file = writeConfig(config);
        assertThrows(IllegalArgumentException.class,
                () -> SimulatorConfigLoader.loadFromFile(file.toString()));
    }

    @Test
    void testIncorrectModelType() throws IOException{
        String config = """
                model=unknown
                totalTime=100
                numSources=2
                alphaOn=1.5
                alphaOff=1.8
                xmOn=1.0
                xmOff=1.0
                hurst=0.8
                """;
        Path file = writeConfig(config);
        assertThrows(IllegalArgumentException.class,
                () -> SimulatorConfigLoader.loadFromFile(file.toString()));
    }

    @Test
    void testHurstLessThanMinimalThreshold() throws IOException{
        String config = """
                model=pareto
                totalTime=100
                numSources=2
                alphaOn=1.5
                alphaOff=1.8
                xmOn=1.0
                xmOff=0
                hurst=0.1
                """;
        Path file = writeConfig(config);
        assertThrows(IllegalArgumentException.class,
                () -> SimulatorConfigLoader.loadFromFile(file.toString()));
    }

    @Test
    void testHurstMoreThanMaximumThreshold() throws IOException{
        String config = """
                model=pareto
                totalTime=100
                numSources=2
                alphaOn=1.5
                alphaOff=1.8
                xmOn=1.0
                xmOff=0
                hurst=2.0
                """;
        Path file = writeConfig(config);
        assertThrows(IllegalArgumentException.class,
                () -> SimulatorConfigLoader.loadFromFile(file.toString()));
    }

}