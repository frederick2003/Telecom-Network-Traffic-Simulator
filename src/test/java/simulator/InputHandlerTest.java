package simulator;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class InputHandlerTest {

    // Utility to create InputHandler with scripted input
    private InputHandler handlerWithInput(String input) {
        InputStream in = new ByteArrayInputStream(input.getBytes());
        Scanner scanner = new Scanner(in);
        return new InputHandler(scanner);
    }

    @Test
    void testGetPositiveDoubleValid() {
        InputHandler inputHandler = handlerWithInput("5.75\n");
        double result = inputHandler.getPositiveDouble("Enter number: ");
        assertEquals(5.75, result);
    }

    @Test
    void testGetPositiveDoubleRetriesOnInvalidThenSucceeds() {
        InputHandler ih = handlerWithInput("abc\n0\n-3\n4.2\n");
        double result = ih.getPositiveDouble("Enter number: ");
        assertEquals(4.2, result);
    }

    @Test
    void testGetPositiveIntRequiresWholeNumber() {
        InputHandler ih = handlerWithInput("3.5\n2\n");
        int result = ih.getPositiveInt("Enter integer: ");
        assertEquals(2, result);
    }

    @Test
    void testCheckQuitThrowsQuitException() {
        InputHandler ih = handlerWithInput("quit\n");
        assertThrows(QuitException.class, () -> ih.checkQuit("quit"));
    }

    @Test
    void testGetSimulatorConfigChoice() {
        InputHandler ih = handlerWithInput("manual\n");
        String choice = ih.getSimulatorConfigChoice();
        assertEquals("manual", choice);
    }

    @Test
    void testLoadConfigFromFileSuccess() {
        // Pretend user types filename
        InputHandler ih = handlerWithInput("docs/parameters.txt\n");

        // IMPORTANT: use a stub for the loader
        SimulatorConfigLoaderStub.successMode = true;

        SimulatorConfig config = ih.loadConfigFromFile();
        assertNotNull(config);
    }

    @Test
    void testLoadConfigFromFileFailure() {
        InputHandler ih = handlerWithInput("missing.txt\n");

        SimulatorConfigLoaderStub.successMode = false;

        SimulatorConfig config = ih.loadConfigFromFile();
        assertNull(config);
    }

    @Test
    void testManualSimulationConfigPareto() {
        String fakeInput = """
                1
                1000
                5
                1.5
                1
                1.4
                1
                """;

        InputHandler ih = handlerWithInput(fakeInput);

        SimulatorConfig cfg = ih.getManualSimulationConfig();

        assertEquals(1000, cfg.getTotalTime());
        assertEquals(5, cfg.getNumberSources());
        assertEquals(1.5, cfg.getAlphaOn());
        assertEquals(1.4, cfg.getAlphaOff());
        assertEquals(1, cfg.getXmOn());
        assertEquals(1, cfg.getXmOff());
        assertEquals(TrafficModelType.PARETO, cfg.getModelType());
    }

    @Test
    void testManualSimulatorConfigGaussianNoise(){
        String fakeInput = """
                2
                0.8
                1000
                5
                1.5
                1
                1.4
                1
                """;

        InputHandler ih = handlerWithInput(fakeInput);

        SimulatorConfig cfg = ih.getManualSimulationConfig();

        assertEquals(1000, cfg.getTotalTime());
        assertEquals(5, cfg.getNumberSources());
        assertEquals(1.5, cfg.getAlphaOn());
        assertEquals(1.4, cfg.getAlphaOff());
        assertEquals(1, cfg.getXmOn());
        assertEquals(1, cfg.getXmOff());
        assertEquals(TrafficModelType.FRACTIONAL_GAUSSIAN_NOISE, cfg.getModelType());
    }

    @Test
    void testManualSimulatorConfigDefaultModel(){
        String fakeInput = """
                3
                1000
                5
                1.5
                1
                1.4
                1
                """;

        InputHandler ih = handlerWithInput(fakeInput);

        SimulatorConfig cfg = ih.getManualSimulationConfig();

        assertEquals(1000, cfg.getTotalTime());
        assertEquals(5, cfg.getNumberSources());
        assertEquals(1.5, cfg.getAlphaOn());
        assertEquals(1.4, cfg.getAlphaOff());
        assertEquals(1, cfg.getXmOn());
        assertEquals(1, cfg.getXmOff());
        assertEquals(TrafficModelType.PARETO, cfg.getModelType());
    }
}
