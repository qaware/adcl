package util;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.support.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MojoTestUtil implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(MojoTestUtil.class);
    private final Path adclPom;
    private final Path localRepo;

    public MojoTestUtil(@NotNull Path adclPom, @NotNull Path localRepo) throws MavenInvocationException {
        this.adclPom = adclPom;
        this.localRepo = localRepo.toAbsolutePath();
        installLocal();
    }

    private void installLocal() throws MavenInvocationException {
        logger.info("Re-versioning as debug for test");
        Utils.callMaven(adclPom, null, null, "versions:set", Pair.of("newVersion", "debug"));
        logger.info("Building adcl jar");
        Utils.callMaven(adclPom, null, "-P junit,!thin", "install");
        logger.info("Installing jar to local repo");
        Utils.callMaven(adclPom, null, null, "install:install-file",
                Pair.of("file", Paths.get("target", "adcl-debug.jar").toString()),
                Pair.of("pomFile", adclPom.toString()),
                Pair.of("localRepositoryPath", localRepo.toString())
        );
    }

    @Override
    public void close() throws Exception {
        logger.info("Reverting version");
        Utils.callMaven(adclPom, null, null, "versions:revert");
        FileUtils.deleteDirectory(localRepo);
    }

    public Pair<Integer, String> runAdclOnPom(Path pomPath) throws MavenInvocationException {
        logger.info("Executing adcl:start");
        return Utils.callMaven(pomPath, null, null, "adcl:start", Pair.of("maven.repo.local", localRepo.toString()));
    }
}
