package com.github.qaware.adcl.util.log;

import org.jetbrains.annotations.NotNull;

public abstract class LogInspector {
    private int allPos, outPos, errPos;

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

    protected abstract String getAll0();

    protected abstract String getOut0();

    protected abstract String getErr0();

    private String get(@NotNull Type type) {
        String all = getAll0(), out = getOut0(), err = getErr0();
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

    enum Type {
        ALL, OUT, ERR
    }
}
