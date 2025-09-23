package io.signoz.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

// Import OpenTelemetry annotations
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.api.trace.SpanKind;

public class SqliteApiServer {

    // SQLite connection URL
    private static final String DB_URL = "jdbc:sqlite:db.sqlite3";

    public static void main(String[] args) throws IOException {
        // Initialize HTTP server on port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Define API endpoint '/items' that returns list of items from SQLite
        server.createContext("/items", new ItemsHandler());

        server.setExecutor(null); // creates a default executor
        System.out.println("Server starting on port 8000...");
        server.start();
    }

    // Handler for /items endpoint
    static class ItemsHandler implements HttpHandler {

        @WithSpan("handle_items_request")
        public void handle(HttpExchange t) throws IOException {
            List<String> items = fetchItemsFromDatabase();

            // Return items as simple JSON array string
            String response = itemsToJson(items);

            t.getResponseHeaders().add("Content-Type", "application/json");
            t.sendResponseHeaders(200, response.getBytes().length);

            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        @WithSpan(value = "database_query", kind = SpanKind.CLIENT)
        private List<String> fetchItemsFromDatabase() {
            List<String> items = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                // Simple query to select item names
                PreparedStatement ps = conn.prepareStatement("SELECT name FROM items");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    items.add(rs.getString("name"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return items;
        }
    }

    // Convert list to JSON array string
    @WithSpan("json_conversion")
    private static String itemsToJson(@SpanAttribute("items") List<String> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < items.size(); i++) {
            sb.append("\"").append(items.get(i)).append("\"");
            if (i < items.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
