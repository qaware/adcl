package util.log;

import org.springframework.boot.test.system.CapturedOutput;

public class CapturedOutputLogInspector extends LogInspector {
    private final CapturedOutput capture;

    public CapturedOutputLogInspector(CapturedOutput capturedOutput) {
        capture = capturedOutput;
    }

    @Override
    protected String getAll0() {
        return capture.getAll();
    }

    @Override
    protected String getOut0() {
        return capture.getOut();
    }

    @Override
    protected String getErr0() {
        return capture.getErr();
    }
}
