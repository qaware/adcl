import core.ApplicationMojoErrorFreeInternalTest;
import core.ApplicationMojoTest;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Wraps JUnit3 tests
 */
public class JUnit3TestWrapper {
    private static final Stream<Class<? extends TestCase>> JUNIT3_TESTCLASSES = Stream.of(
            ApplicationMojoTest.class,
            ApplicationMojoErrorFreeInternalTest.class
    );

    private static final PrintStream NULL_STREAM = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {

        }
    });

    @TestFactory
    Stream<DynamicTest> junit3TestFactory() {
        return JUNIT3_TESTCLASSES.map(clazz -> DynamicTest.dynamicTest("Junit3: " + clazz.getName(), () -> {
            TestResult result = new TestRunner(NULL_STREAM).doRun(new TestSuite(clazz));
            assertNoErrors(result.errors());
            assertNoErrors(result.failures());
        }));
    }

    @SuppressWarnings("RedundantOperationOnEmptyContainer" /* inspection is wrong */)
    private void assertNoErrors(Enumeration<TestFailure> errors) {
        List<TestFailure> list = Collections.list(errors);
        assertThat(list).overridingErrorMessage(list.stream().map(TestFailure::trace).collect(Collectors.joining("\n\n"))).isEmpty();
    }
}
