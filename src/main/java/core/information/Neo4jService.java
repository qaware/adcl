package core.information;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Neo4jService {
    private final InformationRepository infoRepo;
    private final SessionFactory sessionFactory;

    @SuppressWarnings("NotNullFieldNotInitialized" /* gets initialized in constructor */)
    @NotNull
    private RootInformation root;

    @SuppressWarnings("java:S2637" /* gets initialized in constructor */)
    public Neo4jService(InformationRepository infoRepo, SessionFactory sessionFactory) {
        this.infoRepo = infoRepo;
        this.sessionFactory = sessionFactory;
        loadRoot();
    }

    @NotNull
    public RootInformation getRoot() {
        return root;
    }

    @Transactional(readOnly = true)
    public void loadRoot() {
        RootInformation result = null;
        for (Information<?> i : infoRepo.findAll()) {
            if (i instanceof RootInformation) {
                result = (RootInformation) i;
                break;
            }
        }
        if (result == null) result = new RootInformation();
        root = result;
    }

    @Transactional
    public void saveRoot() {
        infoRepo.save(root);
    }

    @Transactional
    public void overrideRoot(@NotNull RootInformation newRoot) {
        Session s = sessionFactory.openSession();
        s.purgeDatabase();
        root = newRoot;
        saveRoot();
    }

    @Repository
    public interface InformationRepository extends Neo4jRepository<Information<?>, Long> {

    }
}
