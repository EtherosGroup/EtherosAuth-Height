package cn.skilfully.etheros.database;

import cn.skilfully.etheros.database.entity.PlayerLocationEntity;
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
public class PlayerLocationDAO {

    @Autowired
    private HibernateUtil hibernateUtil;

    public void create(PlayerLocationEntity location) {
        execute(session -> session.persist(location));
    }

    public Optional<PlayerLocationEntity> findByUuid(UUID uuid) {
        return executeWithResult(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<PlayerLocationEntity> cq = cb.createQuery(PlayerLocationEntity.class);
            Root<PlayerLocationEntity> root = cq.from(PlayerLocationEntity.class);
            cq.select(root).where(cb.equal(root.get("uuid"), uuid));
            return session.createQuery(cq).uniqueResultOptional();
        });
    }

    public void update(PlayerLocationEntity location) {
        execute(session -> session.merge(location));
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
            cq.select(cb.count(cq.from(PlayerLocationEntity.class)));
            return session.createQuery(cq).getSingleResult();
        });
    }

    public boolean existsByUuid(UUID uuid) {
        return executeWithResult(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Long> cq = cb.createQuery(Long.class);
            Root<PlayerLocationEntity> root = cq.from(PlayerLocationEntity.class);
            cq.select(cb.count(root)).where(cb.equal(root.get("uuid"), uuid));
            return session.createQuery(cq).getSingleResult() > 0;
        });
    }

    public List<PlayerLocationEntity> findAll() {
        return executeWithResult(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<PlayerLocationEntity> cq = cb.createQuery(PlayerLocationEntity.class);
            cq.from(PlayerLocationEntity.class);
            return session.createQuery(cq).getResultList();
        });
    }

    public void createOrUpdate(PlayerLocationEntity location) {
        execute(session -> session.merge(location));
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
