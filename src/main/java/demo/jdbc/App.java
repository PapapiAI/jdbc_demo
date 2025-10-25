package demo.jdbc;

import demo.jdbc.dao.StudentDao;
import demo.jdbc.model.Student;
import demo.jdbc.web.JsonUtil;
import demo.jdbc.web.dto.StudentCreateRequest;
import demo.jdbc.web.dto.StudentUpdateRequest;

import static spark.Spark.*;

import java.util.*;

public class App {
    public static void main(String[] args) {
        port(8080);

        // Middleware: JSON & CORS
        after((req, res) -> res.type("application/json"));
        options("/*", (req, res) -> {
            String reqHeaders = req.headers("Access-Control-Request-Headers");
            if (reqHeaders != null) res.header("Access-Control-Allow-Headers", reqHeaders);
            String reqMethod = req.headers("Access-Control-Request-Method");
            if (reqMethod != null) res.header("Access-Control-Allow-Methods", reqMethod);
            return "OK";
        });
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");

            System.out.println(">>> " + req.requestMethod() + " " + req.uri()
                    + (req.raw().getQueryString() != null ? "?" + req.raw().getQueryString() : ""));
            System.out.println("Query keys = " + req.queryParams());
            for (String k : req.queryParams()) {
                System.out.println(" - " + k + " = [" + req.queryParams(k) + "]");
            }
        });

        // Healthcheck
        get("/health", (req, res) -> JsonUtil.toJson(Map.of("ok", true)));

        StudentDao dao = new StudentDao();

        // === CRUD - REST API via Spark ===

        // Get all with filter by email
        get("/students", (req, res) -> {
            String email = req.queryParams("email");

            if (email != null && !email.isBlank()) {
                return dao.findByEmail(email)
                        .<Object>map(JsonUtil::toJson)
                        .orElseGet(() -> {
                            res.status(404);
                            return JsonUtil.toJson(Map.of("error", "Not found"));
                        });
            }
            // fallback
            var list = dao.findAll();
            return JsonUtil.toJson(list);
        });

        // Get by id
        get("/students/:id", (req, res) -> {
            try {
                UUID id = UUID.fromString(req.params(":id"));
                return dao.findById(id)
                        .<Object>map(JsonUtil::toJson)
                        .orElseGet(() -> {
                            res.status(404);
                            return JsonUtil.toJson(Map.of("error", "Not found"));
                        });
            } catch (IllegalArgumentException ex) {
                res.status(400);
                return JsonUtil.toJson(Map.of("error", "Invalid UUID"));
            }
        });

        // Create
        post("/students", (req, res) -> {
            var body = JsonUtil.fromJson(req.body(), StudentCreateRequest.class);

            // Validate
            List<String> errors = new ArrayList<>();
            if (body.fullName == null || body.fullName.isBlank()) errors.add("fullName is required");
            if (body.email == null || !body.email.contains("@")) errors.add("email is invalid");
            if (body.age == null || body.age < 16) errors.add("age must be >= 16");
            if (!errors.isEmpty()) {
                res.status(400);
                return JsonUtil.toJson(Map.of("errors", errors));
            }

            if (dao.existsByEmail(body.email)) {
                res.status(400);
                return JsonUtil.toJson(Map.of("error", "email already exists"));
            }

            Student created = dao.save(body.fullName, body.email, body.age);
            res.status(201);

            String prefix = "http://localhost:8080";
            res.header("Location", prefix + "/students/" + created.id());

            return JsonUtil.toJson(created);
        });

        // Update
        put("/students/:id", (req, res) -> {
            UUID id;
            try {
                id = UUID.fromString(req.params(":id"));
            } catch (IllegalArgumentException ex) {
                res.status(400);
                return JsonUtil.toJson(Map.of("error", "Invalid UUID"));
            }

            var body = JsonUtil.fromJson(req.body(), StudentUpdateRequest.class);

            List<String> errors = new ArrayList<>();
            if (body.fullName == null || body.fullName.isBlank()) errors.add("fullName is required");
            if (body.age == null || body.age < 16) errors.add("age must be >= 16");
            if (!errors.isEmpty()) {
                res.status(400);
                return JsonUtil.toJson(Map.of("errors", errors));
            }

            if (dao.findById(id).isEmpty()) {
                res.status(404);
                return JsonUtil.toJson(Map.of("error", "Not found"));
            }

            Student updated = dao.update(id, body.fullName, body.age);
            return JsonUtil.toJson(updated);
        });

        // Delete
        delete("/students/:id", (req, res) -> {
            try {
                UUID id = UUID.fromString(req.params(":id"));
                boolean ok = dao.deleteById(id);
                if (!ok) {
                    res.status(404);
                    return JsonUtil.toJson(Map.of("error", "Not found"));
                }
                res.status(204);
                return "";
            } catch (IllegalArgumentException ex) {
                res.status(400);
                return JsonUtil.toJson(Map.of("error", "Invalid UUID"));
            }
        });

        // Exception fallback
        exception(Exception.class, (e, req, res) -> {
            res.type("application/json");
            res.status(500);
            res.body(JsonUtil.toJson(Map.of("error", "Internal Server Error", "message", e.getMessage())));
        });
    }
}
