package core.information;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;

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
        Iterator<RootInformation> it = repo.findAll().iterator();
        root = it.hasNext() ? it.next() : new RootInformation();
    }

    @Transactional
    public void saveRoot() {
        repo.save(root);
    }

    @Repository
    public interface RootRepository extends Neo4jRepository<RootInformation, Long> {
    }
}
