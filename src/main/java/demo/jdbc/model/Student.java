package demo.jdbc.model;

import java.time.Instant;
import java.util.UUID;

public record Student(
        UUID id,
        String fullName,
        String email,
        Integer age,
        Instant createdAt) {}
