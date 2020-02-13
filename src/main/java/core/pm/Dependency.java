package core.pm;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.StringJoiner;

public class Dependency {
    @NotNull
    private final String name;

    @NotNull
    private final String version;

    @NotNull
    private final String scope;

    public Dependency(@NotNull String name, @NotNull String version, @NotNull String scope) {
        this.name = name.replace('.', '-');
        this.version = version;
        this.scope = scope;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getVersion() {
        return version;
    }

    @NotNull
    public String getScope() {
        return scope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dependency)) return false;
        Dependency that = (Dependency) o;
        return name.equals(that.name) &&
                version.equals(that.version) &&
                scope.equals(that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, scope);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Dependency.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("version='" + version + "'")
                .add("scope='" + scope + "'")
                .toString();
    }
}
