package com.github.qaware.adcl.pm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DependencyTest {
    @Test
    void test() {
        Dependency a = new Dependency("a", "b", "c");
        Dependency b = new Dependency("a", "b", "c");
        Dependency c = new Dependency("", "", "");

        assertThat(a).isEqualTo(a);
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).hasSameHashCodeAs(b);
        assertThat(a.getName()).isEqualTo("a");
        assertThat(a.getVersion()).isEqualTo("b");
        assertThat(a.getScope()).isEqualTo("c");
        assertThat(a).hasToString("Dependency[name='a', version='b', scope='c']");
    }
}
