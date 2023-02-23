package uj.wmii.pwj.gvt;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GvtTest {

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
    void prepareOutput() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        out = new ByteArrayOutputStream(512);
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
    }

    private void executeRuntime(String command, String failMessage) {
        try {
            Runtime.getRuntime().exec(command);
            Thread.sleep(10);
        } catch (IOException | InterruptedException e) {
            fail(failMessage, e);
        }
    }

    private static void safeDelete(Path... paths) {
        for (var p: paths) {
            try {
                Files.delete(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @AfterAll
    static void cleanUp() {
        Path gvtPath = Path.of(".gvt");
        try {
            Files.walk(gvtPath)
                .sorted(Comparator.reverseOrder())
                .forEach(GvtTest::safeDelete);
        } catch (IOException e) {
            e.printStackTrace();
        }
        safeDelete(Path.of("a.txt"), Path.of("b.txt"), Path.of("c.txt"));
    }

    @Test
    @Order(1)
    public void invokeEmptyCommand() {
        Gvt gvt = new Gvt(new TestExitHandler(1, "Please specify command."));
        gvt.mainInternal();
    }

    @Test
    @Order(2)
    public void addFileToNotInitializedRepo() {
        Gvt gvt = new Gvt(new TestExitHandler(-2, "Current directory is not initialized. Please use init command to initialize."));
        gvt.mainInternal("add", "a.txt");
    }

    @Test
    @Order(3)
    public void initializeRepo() {
        Gvt gvt = new Gvt(new TestExitHandler(0, "Repository initialized successfully."));
        gvt.mainInternal("init");
    }

    @Test
    @Order(4)
    public void addNotExistingFileToInitializedRepo() {
        Gvt gvt = new Gvt(new TestExitHandler(21, "File not found. File: a.txt"));
        gvt.mainInternal("add", "a.txt");
    }

    @Test
    @Order(5)
    public void checkVersion0() {
        Gvt gvt = new Gvt(new TestExitHandler(0, "Version: 0\nGVT initialized."));
        gvt.mainInternal("version");
    }

    @Test
    @Order(6)
    public void addFileToRepo() {
        executeRuntime("touch a.txt", "File a.txt cannot be created");
        Gvt gvt = new Gvt(new TestExitHandler(0, "File added successfully. File: a.txt"));
        gvt.mainInternal("add", "a.txt");
    }

    @Test
    @Order(7)
    public void checkVersion1() {
        Gvt gvt = new Gvt(new TestExitHandler(0, "Version: 1\nAdded file: a.txt"));
        gvt.mainInternal("version");
    }

    @Test
    @Order(8)
    public void addSecondFileToRepo() {
        executeRuntime("touch b.txt", "File b.txt cannot be created");
        Gvt gvt = new Gvt(new TestExitHandler(0, "File added successfully. File: b.txt"));
        gvt.mainInternal("add", "b.txt");
    }

    @Test
    @Order(9)
    public void checkVersion2() {
        Gvt gvt = new Gvt(new TestExitHandler(0, "Version: 2\nAdded file: b.txt"));
        gvt.mainInternal("version");
    }

    @Test
    @Order(10)
    public void detachNoFile() {
        Gvt gvt = new Gvt(new TestExitHandler(30, "Please specify file to detach."));
        gvt.mainInternal("detach");
    }

    @Test
    @Order(11)
    public void detachNotExistingFile() {
        Gvt gvt = new Gvt(new TestExitHandler(0, "File is not added to gvt. File: x"));
        gvt.mainInternal("detach", "x");
    }

    @Test
    @Order(12)
    public void detachExistingFile() {
        Gvt gvt = new Gvt(new TestExitHandler(0, "File detached successfully. File: b.txt"));
        gvt.mainInternal("detach", "b.txt");
    }

    @Test
    @Order(13)
    public void addAgainDetachedFileExistingFile() {
        Gvt gvt = new Gvt(new TestExitHandler(0, "File added successfully. File: b.txt"));
        gvt.mainInternal("add", "b.txt");
    }

    @Test
    @Order(14)
    public void tryCommitNoFile() {
        Gvt gvt = new Gvt(new TestExitHandler(50, "Please specify file to commit."));
        gvt.mainInternal("commit");
    }

    @Test
    @Order(15)
    public void tryCommitNotExistingFile() {
        Gvt gvt = new Gvt(new TestExitHandler(51, "File not found. File: c.txt"));
        gvt.mainInternal("commit", "c.txt");
    }

    @Test
    @Order(16)
    public void tryCommitNotAddedFile() {
        executeRuntime("touch c.txt", "File c.txt cannot be created");
        Gvt gvt = new Gvt(new TestExitHandler(0, "File is not added to gvt. File: c.txt"));
        gvt.mainInternal("commit", "c.txt");
    }

    @Test
    @Order(17)
    public void modifyAndCommitFile() {
        executeRuntime("echo \"Ala ma kota\" > b.txt", "Cannot modify file b.txt");
        Gvt gvt = new Gvt(new TestExitHandler(0, "File committed successfully. File: b.txt"));
        gvt.mainInternal("commit", "b.txt");
    }

    @Test
    @Order(18)
    public void checkVersion4() {
        Gvt gvt = new Gvt(new TestExitHandler(0, "Version: 4\nCommitted file: b.txt"));
        gvt.mainInternal("version");
    }

    @Test
    @Order(19)
    public void history() {
        Gvt gvt = new Gvt(new TestExitHandler(0, "Added file: b.txt\nDetached file: b.txt"));
        gvt.mainInternal("history", "2");
    }

    @Test
    @Order(20)
    public void checkoutInvalidVersion() {
        Gvt gvt = new Gvt(new TestExitHandler(40, "Invalid version number: 20"));
        gvt.mainInternal("checkout", "20");
    }

    @Test
    @Order(21)
    public void checkoutVersion2() {
        Gvt gvt = new Gvt(new TestExitHandler(0, "Version 2 checked out successfully."));
        gvt.mainInternal("checkout", "2");
    }
}
