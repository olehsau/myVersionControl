package uj.wmii.pwj.gvt;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class AltGvtTest {

    private ByteArrayOutputStream out;

    private class TestExitHandler extends ExitHandler {

        private final int expectedCode;
        private final String expectedMessage;

        TestExitHandler(int expectedCode, String expectedMessage) {
            this.expectedCode = expectedCode;
            this.expectedMessage = expectedMessage;
        }

        @Override
        void exitOperation(int code) {
            assertThat(out.toString()).isEqualToIgnoringNewLines(expectedMessage);
            assertThat(code).isEqualTo(expectedCode);
        }
    }

    @BeforeEach
    public void prepareOutput() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        out = new ByteArrayOutputStream(512);
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
    }

    private static void safeDelete(Path p) {
        try {
            Files.delete(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    static void cleanUp() {
        Path gvtPath = Path.of(".gvt");
        try {
            Files.walk(gvtPath)
                    .sorted(Comparator.reverseOrder())
                    .forEach(AltGvtTest::safeDelete);
        } catch (IOException e) {
            e.printStackTrace();
        }
        safeDelete(Path.of("a.txt"));
    }

    @ParameterizedTest(name = "{index} {0}")
    @CsvFileSource(resources = "/test.csv", numLinesToSkip = 1)
    @Disabled
    public void testCase(
        String name, boolean hasRuntimeCommand, String runtimeCommand, int expectedExitCode, String expectedExitMessage, String commands, boolean hasComment, String comment) {
        if (hasRuntimeCommand) {
            try {
                Runtime.getRuntime().exec(runtimeCommand);
            } catch (IOException e) {
                fail("Error with runtime command: " + runtimeCommand, e);
            }
        }
        Gvt gvt = new Gvt(new TestExitHandler(expectedExitCode, expectedExitMessage));
        String[] pureCommands = commands != null ? commands.split(" ") : new String[0];
        String[] commandsArray;
        if (hasComment) {
            commandsArray = new String[pureCommands.length + 2];
            System.arraycopy(pureCommands, 0, commandsArray, 0, pureCommands.length);
            commandsArray[commandsArray.length - 2] = "-m";
            commandsArray[commandsArray.length - 1] = comment;
        } else {
            commandsArray = pureCommands;
        }
        gvt.mainInternal(commandsArray);
    }

}
