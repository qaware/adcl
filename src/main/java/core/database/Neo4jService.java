package core.database;

import core.information.Information;
import core.information.RootInformation;
import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * ADCLs database service. Holds the root node represented by the database
 */
@Service
public class Neo4jService {
    private final InformationRepository infoRepo;
    private final SessionFactory sessionFactory;

    @SuppressWarnings("NotNullFieldNotInitialized" /* gets initialized in constructor */)
    @NotNull
    private RootInformation root;

    /**
     * Service init by spring. Instantly loads the root
     *
     * @param infoRepo       the information repository bean
     * @param sessionFactory the neo4j driver session factory
     */
    @SuppressWarnings("java:S2637" /* gets initialized in constructor */)
    public Neo4jService(InformationRepository infoRepo, SessionFactory sessionFactory) {
        this.infoRepo = infoRepo;
        this.sessionFactory = sessionFactory;
        loadRoot();
    }

    /**
     * @return the current root
     */
    @NotNull
    public RootInformation getRoot() {
        return root;
    }

    /**
     * (re)load the root from database, which can be retrieved by {@link Neo4jService#getRoot()}
     */
    @Transactional(readOnly = true)
    public void loadRoot() {
        root = StreamSupport.stream(infoRepo.findAll().spliterator(), true)
                .filter(RootInformation.class::isInstance).findAny()
                .map(RootInformation.class::cast).orElseGet(RootInformation::new);
    }

    /**
     * save current root to database
     */
    @Transactional
    public void saveRoot() {
        Set<Information<?>> all = root.getAllChildren(null);
        all.add(root);
        all.forEach(Purgeable::purgeIds);

        Session session = sessionFactory.openSession();
        session.purgeDatabase();
        Transaction transaction = session.beginTransaction();
        session.save(all, 0);
        session.save(all.stream().flatMap(Information::getOutgoingRelations).collect(Collectors.toList()), 0);
        transaction.commit();
        transaction.close();
    }

    /**
     * override the current root with a new root. Purges database and saves the new data to it
     *
     * @param newRoot the new root
     */
    @Transactional
    public void overrideRoot(@NotNull RootInformation newRoot) {
        root = newRoot;
        saveRoot();
    }

    /**
     * Information repository DAO
     */
    @Repository
    public interface InformationRepository extends Neo4jRepository<Information<?>, Long> {

    }
}
