package uj.wmii.pwj.gvt;

public class ExitHandler {

    final void exit(int code, String message) {
        System.out.println(message);
        exitOperation(code);
    }

    void exitOperation(int code) {
        System.exit(code);
    }

}
