package core.pm;

import org.jetbrains.annotations.NotNull;

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
}
