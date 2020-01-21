package core;

import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationMojoTest {
    private static final PrintStream NULL_STREAM = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {

        }
    });

    @Test
    void testApplicationMojo() {
        TestResult result = new TestRunner(NULL_STREAM).doRun(new TestSuite(ApplicationMojoTestInternal.class));
        assertNoErrors(result.errors());
        assertNoErrors(result.failures());
    }

    @SuppressWarnings("RedundantOperationOnEmptyContainer" /* inspection is wrong */)
    private void assertNoErrors(Enumeration<TestFailure> errors) {
        List<TestFailure> list = Collections.list(errors);
        assertThat(list).overridingErrorMessage(list.stream().map(TestFailure::trace).collect(Collectors.joining("\n\n"))).isEmpty();
    }

}
