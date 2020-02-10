package util;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.support.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MojoTestUtil implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(MojoTestUtil.class);
    private final Path localRepo;

    private MojoTestUtil(Path localRepo) {
        this.localRepo = localRepo;
    }

    @NotNull
    @Contract("_, _ -> new")
    public static MojoTestUtil installLocal(@NotNull Path pom, @NotNull Path localRepo) throws MavenInvocationException {
        localRepo = localRepo.toAbsolutePath();
        logger.info("Building adcl jar");
        Utils.callMaven(pom, null, "-P junit,!thin", "install");
        logger.info("Installing jar to local repo");
        Utils.callMaven(pom, null, null, "install:install-file",
                Pair.of("file", Paths.get("target", "adcl-debug.jar").toString()),
                Pair.of("pomFile", pom.toString()),
                Pair.of("localRepositoryPath", localRepo.toString())
        );
        return new MojoTestUtil(localRepo);
    }

    @Override
    public void close() throws Exception {
        FileUtils.deleteDirectory(localRepo);
    }

    public Pair<Integer, String> runAdclOnPom(Path pomPath) throws MavenInvocationException {
        logger.info("Executing adcl:start");
        return Utils.callMaven(pomPath, null, null, "adcl:start", Pair.of("maven.repo.local", localRepo.toString()));
    }
}
