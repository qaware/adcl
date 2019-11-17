package de.thbingen.adcl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SuppressWarnings("WeakerAccess")
public class ExampleTest {
    @Test
    public void testHello() {
        assertFalse(new Example().fun(1, 1));
    }
}
