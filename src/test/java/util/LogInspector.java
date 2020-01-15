package util;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.system.CapturedOutput;

public class LogInspector {
    private final CapturedOutput capture;
    private int allPos, outPos, errPos;

    public LogInspector(CapturedOutput capturedOutput) {
        this.capture = capturedOutput;
    }

    public String getAll() {
        return get(Type.ALL);
    }

    public String getErr() {
        return get(Type.ERR);
    }

    public String getOut() {
        return get(Type.OUT);
    }

    public String getNewAll() {
        int pos = allPos; // do not inline, allPos gets updated in getAll()
        return getAll().substring(pos);
    }

    public String getNewErr() {
        int pos = errPos; // do not inline, allPos gets updated in getAll()
        return getErr().substring(pos);
    }

    public String getNewOut() {
        int pos = outPos; // do not inline, allPos gets updated in getAll()
        return getOut().substring(pos);
    }

    private String get(@NotNull Type type) {
        String all = capture.getAll(), out = capture.getOut(), err = capture.getErr();
        allPos = all.length();
        outPos = out.length();
        errPos = err.length();
        switch (type) {
            case ALL:
                return all;
            case OUT:
                return out;
            case ERR:
                return err;
        }
        throw new IllegalStateException();
    }

    private enum Type {
        ALL, OUT, ERR
    }
}
