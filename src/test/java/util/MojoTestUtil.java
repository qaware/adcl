package util;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MojoTestUtil implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(MojoTestUtil.class);
    private final Path adclPom;

    public MojoTestUtil(@NotNull Path adclPom) throws MavenInvocationException, IOException {
        this.adclPom = adclPom;
        installLocal();
    }

    private void installLocal() throws MavenInvocationException, IOException {
        logger.info("Removing old installed adcl artifacts");
        String m2repo = Utils.callMaven(Paths.get("pom.xml"), null, "-q", "help:evaluate", Pair.of("expression", "settings.localRepository"), Pair.of("forceStdout", "true")).getValue();
        Utils.delete(Paths.get(m2repo.substring(0, m2repo.length() - 2), "com", "github", "qaware"));
        logger.info("Re-versioning as debug for test");
        Utils.callMaven(adclPom, null, null, "versions:set", Pair.of("newVersion", "debug"));
        logger.info("Building adcl jar");
        Utils.callMaven(adclPom, null, "-P junit,!thin", "install");
        logger.info("Installing jar to local repo");
        Utils.callMaven(adclPom, null, null, "install:install-file",
                Pair.of("file", Paths.get("target", "adcl-debug.jar").toString()),
                Pair.of("pomFile", adclPom.toString())
        );
    }

    @Override
    public void close() throws Exception {
        logger.info("Reverting version");
        Utils.callMaven(adclPom, null, null, "versions:revert");
    }

    public Pair<Integer, String> runAdclOnPom(Path pomPath) throws MavenInvocationException {
        logger.info("Executing adcl:start");
        return Utils.callMaven(pomPath, null, null, "adcl:start");
    }
}
