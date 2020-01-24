package core.information2;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Neo4jService {
    private final RootRepository repo;

    @SuppressWarnings("NotNullFieldNotInitialized" /* gets initialized in constructor */)
    @NotNull
    private RootInformation root;

    @SuppressWarnings("java:S2637" /* gets initialized in constructor */)
    public Neo4jService(RootRepository repo) {
        this.repo = repo;
        loadRoot();
    }

    @NotNull
    public RootInformation getRoot() {
        return root;
    }

    @Transactional(readOnly = true)
    public void loadRoot() {
        root = repo.findAll().iterator().next();
    }

    @Transactional
    public void saveRoot(RootInformation root) {
        repo.save(root);
    }

    @Repository
    interface RootRepository extends Neo4jRepository<RootInformation, Long> {
    }
}
