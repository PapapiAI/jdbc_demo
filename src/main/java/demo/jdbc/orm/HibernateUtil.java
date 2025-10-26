package demo.jdbc.orm;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration config = new Configuration().configure();
            return config.buildSessionFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build SessionFactory", e);
        }
    }

    private HibernateUtil() {}

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void closeSession() {
        sessionFactory.close();
    }
}
