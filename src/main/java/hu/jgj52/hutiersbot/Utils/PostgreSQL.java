package hu.jgj52.hutiersbot.Utils;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class PostgreSQL {
    private final HikariDataSource dataSource;
    private final ExecutorService executor;

    public PostgreSQL(String host, int port, String database, String username, String password) {
        this.executor = Executors.newFixedThreadPool(4);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d/%s", host, port, database));
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        config.setDriverClassName("org.postgresql.Driver");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL Driver not found in classpath", e);
        }

        this.dataSource = new HikariDataSource(config);
    }


    public QueryBuilder from(String table) {
        return new QueryBuilder(this.dataSource, table);
    }

    public CompletableFuture<QueryResult> query(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }

                ResultSet rs = stmt.executeQuery();
                List<Map<String, Object>> data = new ArrayList<>();

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnName(i), rs.getObject(i));
                    }
                    data.add(row);
                }

                return new QueryResult(data, null);
            } catch (SQLException e) {
                return new QueryResult(new ArrayList<>(), e.getMessage());
            }
        }, executor);
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    public static class QueryBuilder {
        private final HikariDataSource dataSource;
        private final ExecutorService executor = Executors.newCachedThreadPool();
        private String table;
        private String selected = "*";
        private List<FilterClause> filters = new ArrayList<>();
        private String orderClause = "";

        public QueryBuilder(HikariDataSource dataSource, String table) {
            this.dataSource = dataSource;
            this.table = "\"" + table + "\"";
        }

        private QueryBuilder(HikariDataSource dataSource) {
            this.dataSource = dataSource;
        }

        public QueryBuilder select(String columns) {
            QueryBuilder newBuilder = this.clone();
            newBuilder.selected = columns;
            return newBuilder;
        }

        public QueryBuilder eq(String column, Object value) {
            QueryBuilder newBuilder = this.clone();
            newBuilder.filters.add(new FilterClause(column, value));
            return newBuilder;
        }

        public QueryBuilder order(String column, boolean ascending) {
            QueryBuilder newBuilder = this.clone();
            newBuilder.orderClause = String.format("ORDER BY \"%s\" %s", column, ascending ? "ASC" : "DESC");
            return newBuilder;
        }

        public QueryBuilder order(String column) {
            return order(column, true);
        }

        public QueryBuilder clone() {
            QueryBuilder newBuilder = new QueryBuilder(this.dataSource);
            newBuilder.table = this.table;
            newBuilder.selected = this.selected;
            newBuilder.filters = new ArrayList<>(this.filters);
            newBuilder.orderClause = this.orderClause;
            return newBuilder;
        }

        private WhereClause buildWhereClause() {
            List<String> conditions = new ArrayList<>();
            List<Object> values = new ArrayList<>();

            for (int i = 0; i < filters.size(); i++) {
                FilterClause filter = filters.get(i);
                conditions.add(String.format("\"%s\" = ?", filter.column));
                values.add(filter.value);
            }

            String where = conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions);
            return new WhereClause(where, values);
        }

        public CompletableFuture<QueryResult> single() {
            return CompletableFuture.supplyAsync(() -> {
                WhereClause whereClause = buildWhereClause();
                String sql = String.format("SELECT %s FROM %s %s LIMIT 1", selected, table, whereClause.where);

                try (Connection conn = dataSource.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {

                    for (int i = 0; i < whereClause.values.size(); i++) {
                        stmt.setObject(i + 1, whereClause.values.get(i));
                    }

                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        ResultSetMetaData metaData = rs.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        Map<String, Object> row = new HashMap<>();

                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnName(i), rs.getObject(i));
                        }

                        return new QueryResult(Arrays.asList(row), null);
                    }

                    return new QueryResult(new ArrayList<>(), null);
                } catch (SQLException e) {
                    return new QueryResult(new ArrayList<>(), e.getMessage());
                }
            }, executor);
        }

        public CompletableFuture<QueryResult> delete() {
            return CompletableFuture.supplyAsync(() -> {
                WhereClause whereClause = buildWhereClause();
                String sql = String.format("DELETE FROM %s %s RETURNING *", table, whereClause.where);

                try (Connection conn = dataSource.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {

                    for (int i = 0; i < whereClause.values.size(); i++) {
                        stmt.setObject(i + 1, whereClause.values.get(i));
                    }

                    ResultSet rs = stmt.executeQuery();
                    List<Map<String, Object>> data = new ArrayList<>();

                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnName(i), rs.getObject(i));
                        }
                        data.add(row);
                    }

                    return new QueryResult(data, null);
                } catch (SQLException e) {
                    return new QueryResult(new ArrayList<>(), e.getMessage());
                }
            }, executor);
        }

        public CompletableFuture<QueryResult> insert(Map<String, Object> data) {
            return CompletableFuture.supplyAsync(() -> {
                List<String> keys = new ArrayList<>(data.keySet());
                List<Object> values = new ArrayList<>(data.values());

                String columns = String.join(", ", keys);
                String placeholders = String.join(", ", Collections.nCopies(keys.size(), "?"));
                String sql = String.format("INSERT INTO %s (%s) VALUES (%s) RETURNING *", table, columns, placeholders);

                try (Connection conn = dataSource.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {

                    for (int i = 0; i < values.size(); i++) {
                        stmt.setObject(i + 1, values.get(i));
                    }

                    ResultSet rs = stmt.executeQuery();
                    List<Map<String, Object>> result = new ArrayList<>();

                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnName(i), rs.getObject(i));
                        }
                        result.add(row);
                    }

                    return new QueryResult(result, null);
                } catch (SQLException e) {
                    return new QueryResult(new ArrayList<>(), e.getMessage());
                }
            }, executor);
        }

        public CompletableFuture<QueryResult> update(Map<String, Object> data) {
            return CompletableFuture.supplyAsync(() -> {
                if (data == null || data.isEmpty()) {
                    return new QueryResult(new ArrayList<>(), "No update data provided");
                }

                WhereClause whereClause = buildWhereClause();

                List<String> setClauses = new ArrayList<>();
                List<Object> setValues = new ArrayList<>();

                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    setClauses.add(String.format("\"%s\" = ?", entry.getKey()));
                    setValues.add(entry.getValue());
                }

                List<Object> allValues = new ArrayList<>(setValues);
                allValues.addAll(whereClause.values);

                String sql = String.format("UPDATE %s SET %s %s RETURNING *",
                        table, String.join(", ", setClauses), whereClause.where);

                try (Connection conn = dataSource.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {

                    for (int i = 0; i < allValues.size(); i++) {
                        stmt.setObject(i + 1, allValues.get(i));
                    }

                    ResultSet rs = stmt.executeQuery();
                    List<Map<String, Object>> result = new ArrayList<>();

                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnName(i), rs.getObject(i));
                        }
                        result.add(row);
                    }

                    return new QueryResult(result, null);
                } catch (SQLException e) {
                    return new QueryResult(new ArrayList<>(), e.getMessage());
                }
            }, executor);
        }

        public CompletableFuture<QueryResult> execute() {
            return CompletableFuture.supplyAsync(() -> {
                WhereClause whereClause = buildWhereClause();
                String sql = String.format("SELECT %s FROM %s %s %s", selected, table, whereClause.where, orderClause);

                try (Connection conn = dataSource.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {

                    for (int i = 0; i < whereClause.values.size(); i++) {
                        stmt.setObject(i + 1, whereClause.values.get(i));
                    }

                    ResultSet rs = stmt.executeQuery();
                    List<Map<String, Object>> data = new ArrayList<>();

                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnName(i), rs.getObject(i));
                        }
                        data.add(row);
                    }

                    return new QueryResult(data, null);
                } catch (SQLException e) {
                    return new QueryResult(new ArrayList<>(), e.getMessage());
                }
            }, executor);
        }
    }

    public static class QueryResult {
        public final List<Map<String, Object>> data;
        public final String error;

        public QueryResult(List<Map<String, Object>> data, String error) {
            this.data = data;
            this.error = error;
        }

        public boolean hasError() {
            return error != null;
        }

        public boolean isEmpty() {
            return data == null || data.isEmpty();
        }

        public Map<String, Object> first() {
            return isEmpty() ? null : data.get(0);
        }
    }

    private static class FilterClause {
        final String column;
        final Object value;

        FilterClause(String column, Object value) {
            this.column = column;
            this.value = value;
        }
    }

    private static class WhereClause {
        final String where;
        final List<Object> values;

        WhereClause(String where, List<Object> values) {
            this.where = where;
            this.values = values;
        }
    }
}
