package util.log;

import org.apache.maven.plugin.logging.Log;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import util.MultiWriter;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ApacheLogLogInspector extends LogInspector {
    private final PrintWriter pout, perr;
    public final Log logger = createLogger();
    private final StringWriter sall, sout, serr;

    public ApacheLogLogInspector() {
        sall = new StringWriter();
        pout = new PrintWriter(new MultiWriter(sall, sout = new StringWriter()));
        perr = new PrintWriter(new MultiWriter(sall, serr = new StringWriter()));
    }

    @Override
    protected String getAll0() {
        return sall.toString();
    }

    @Override
    protected String getOut0() {
        return sout.toString();
    }

    @Override
    protected String getErr0() {
        return serr.toString();
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    private Log createLogger() {
        return new Log() {
            @Override
            public boolean isDebugEnabled() {
                return false;
            }

            @Override
            public void debug(CharSequence content) {
                //NOP
            }

            @Override
            public void debug(CharSequence content, Throwable error) {
                //NOP
            }

            @Override
            public void debug(Throwable error) {
                //NOP
            }

            @Override
            public boolean isInfoEnabled() {
                return true;
            }

            @Override
            public void info(CharSequence content) {
                info(content, null);
            }

            @Override
            public void info(CharSequence content, Throwable error) {
                pout.println(content);
                if (error != null) error.printStackTrace(pout);
            }

            @Override
            public void info(Throwable error) {
                info(null, error);
            }

            @Override
            public boolean isWarnEnabled() {
                return true;
            }

            @Override
            public void warn(CharSequence content) {
                warn(content, null);
            }

            @Override
            public void warn(CharSequence content, Throwable error) {
                pout.println(content);
                if (error != null) error.printStackTrace(pout);
            }

            @Override
            public void warn(Throwable error) {
                warn(null, error);
            }

            @Override
            public boolean isErrorEnabled() {
                return true;
            }

            @Override
            public void error(CharSequence content) {
                error(content, null);
            }

            @Override
            public void error(CharSequence content, Throwable error) {
                perr.println(content);
                if (error != null) error.printStackTrace(perr);
            }

            @Override
            public void error(Throwable error) {
                error(null, error);
            }
        };
    }
}
