package demo.jdbc.dao;

import demo.jdbc.model.orm.StudentEntity;
import demo.jdbc.orm.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HibernateStudentDao {
    public List<StudentEntity> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Hibernate Query Language (HQL)
            final Query<StudentEntity> query = session.createQuery(
                    "from StudentEntity s order by s.createdAt desc", StudentEntity.class
            );

            // Execute query and get a list of StudentEntity
            return query.getResultList();
        }
    }

    public Optional<StudentEntity> findById(UUID id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // session.get(): only using for primary key
            return Optional.ofNullable(session.get(StudentEntity.class, id));
        }
    }

    public Optional<StudentEntity> findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            final Query<StudentEntity> query = session.createQuery(
                    "from StudentEntity s where s.email = :email", StudentEntity.class
            );
            query.setParameter("email", email);

            return query.uniqueResultOptional();
        }
    }

    public boolean existsByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            final Query<Long> query = session.createQuery(
                    "select count(s) from StudentEntity s where s.email = :email", Long.class
            );
            query.setParameter("email", email);

            // query.getSingleResult(): dùng khi kết quả query DB chắc chắn trả về ít nhất 1 bản ghi, ví dụ select count()
            return query.getSingleResult() > 0;
        }
    }

    public StudentEntity save(String fullName, String email, Integer age) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            StudentEntity student = new StudentEntity();
            student.setFullName(fullName);
            student.setEmail(email);
            student.setAge(age);

            // Ở class StudentEntity, cần Hibernate tự động sinh id
            // nếu không Hibernate sẽ insert vào bảng students ở DB với id là null -> xung đột DB
            // -> khiến Hibernate ném lỗi "LogicalConnectionManagedImpl … is closed” khi cố thực hiện session.persist(student)
            session.persist(student);

            // Cần refresh() để select lại student ở DB với created_at = now()
            session.flush();
            session.refresh(student);

            transaction.commit();
            return student;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    public StudentEntity update(UUID id, String fullName, Integer age) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            StudentEntity student = session.get(StudentEntity.class, id);
            if (student == null) throw new RuntimeException("Not found student with id: " + id);

            student.setFullName(fullName);
            student.setAge(age);

            // Cần refresh() để select lại student ở DB với trigger updated_at
            session.flush();
            session.refresh(student);

            transaction.commit();
            return student;
        } catch (Exception e) {
            if (transaction !=null) transaction.rollback();
            throw e;
        }
    }

    public boolean deleteById(UUID id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            StudentEntity student = session.get(StudentEntity.class, id);
            if (student == null) {
                transaction.rollback();
                return false;
            }
            session.remove(student);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }
}
