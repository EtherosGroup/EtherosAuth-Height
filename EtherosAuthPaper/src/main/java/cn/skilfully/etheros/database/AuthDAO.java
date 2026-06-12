package cn.skilfully.etheros.database;

import cn.skilfully.etheros.database.entity.PlayerAccountEntity;
import cn.skilfully.etheros.etherosframework.di.annotation.Autowired;
import cn.skilfully.etheros.etherosframework.di.annotation.Service;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthDAO {

    @Autowired
    private HibernateUtil hibernateUtil;

    public void create(PlayerAccountEntity account) {
        execute(session -> session.persist(account));
    }

    public Optional<PlayerAccountEntity> findByUuid(UUID uuid) {
        return executeWithResult(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<PlayerAccountEntity> cq = cb.createQuery(PlayerAccountEntity.class);
            Root<PlayerAccountEntity> root = cq.from(PlayerAccountEntity.class);
            cq.select(root).where(cb.equal(root.get("uuid"), uuid));
            return session.createQuery(cq).uniqueResultOptional();
        });
    }

    public void update(PlayerAccountEntity account) {
        execute(session -> session.merge(account));
    }

    public void deleteByUuid(UUID uuid) {
        execute(session -> {
            findByUuid(uuid).ifPresent(session::remove);
        });
    }

    public long count() {
        return executeWithResult(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            cq.select(cb.count(cq.from(PlayerAccountEntity.class)));
            return session.createQuery(cq).getSingleResult();
        });
    }

    public boolean existsByUuid(UUID uuid) {
        return executeWithResult(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<PlayerAccountEntity> root = cq.from(PlayerAccountEntity.class);
            cq.select(cb.count(root)).where(cb.equal(root.get("uuid"), uuid));
            return session.createQuery(cq).getSingleResult() > 0;
        });
    }

    public List<PlayerAccountEntity> findAll() {
        return executeWithResult(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<PlayerAccountEntity> cq = cb.createQuery(PlayerAccountEntity.class);
            cq.from(PlayerAccountEntity.class);
            return session.createQuery(cq).getResultList();
        });
    }

    public void createOrUpdate(PlayerAccountEntity account) {
        execute(session -> session.merge(account));
    }

    private void execute(SessionConsumer action) {
        SessionFactory sf = hibernateUtil.getSessionFactory();
        try (Session session = sf.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                action.accept(session);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }

    private <T> T executeWithResult(SessionFunction<T> action) {
        SessionFactory sf = hibernateUtil.getSessionFactory();
        try (Session session = sf.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                T result = action.apply(session);
                tx.commit();
                return result;
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }

    @FunctionalInterface
    private interface SessionConsumer {
        void accept(Session session);
    }

    @FunctionalInterface
    private interface SessionFunction<T> {
        T apply(Session session);
    }
}
