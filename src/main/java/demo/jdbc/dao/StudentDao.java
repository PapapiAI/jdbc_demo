package demo.jdbc.dao;

import demo.jdbc.model.Student;
import demo.jdbc.db.DB;

import java.sql.*;
import java.util.*;

public class StudentDao {
    public List<Student> findAll() {
        String sql = """
                SELECT id, full_name, email, age, created_at
                FROM app.students
                ORDER BY created_at DESC
                """;
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()
        ) {
            List<Student> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
    }

    public Optional<Student> findById(UUID id) {
        String sql = """
                SELECT id, full_name, email, age, created_at
                FROM app.students
                WHERE id = ?
                """;
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
    }

    public Optional<Student> findByEmail(String email) {
        String sql = """
                SELECT id, full_name, email, age, created_at
                FROM app.students
                WHERE email = ?
                """;
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM app.students WHERE email = ?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
    }

    public Student save(String fullName, String email, Integer age) {
        String sql = """
                INSERT INTO app.students(full_name, email, age)
                VALUES (?, ?, ?)
                RETURNING id, full_name, email, age, created_at
                """;
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            if (age == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, age);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
    }

    public Student update(UUID id, String fullName, Integer age) {
        String sql = """
                UPDATE app.students
                SET full_name = ?, age = ?
                WHERE id = ?
                RETURNING id, full_name, email, age, created_at
                """;
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setString(1, fullName);
            if (age == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, age);
            ps.setObject(3, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new RuntimeException("Not found student with id: " + id);
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
    }

    public boolean deleteById(UUID id) {
        String sql = "DELETE FROM app.students WHERE id = ?";
        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setObject(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("DB error: " + e.getMessage(), e);
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
                (UUID) rs.getObject("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                (Integer) rs.getObject("age"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
