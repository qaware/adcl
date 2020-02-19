package com.github.qaware.adcl.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;

public class MultiWriter extends Writer {
    private final Writer[] writers;

    public MultiWriter(Writer... writers) {
        this.writers = writers;
    }

    @Override
    public void write(@NotNull char[] cbuf, int off, int len) throws IOException {
        for (Writer w : writers) w.write(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        for (Writer w : writers) w.flush();
    }

    @Override
    public void close() throws IOException {
        for (Writer w : writers) w.close();
    }
}