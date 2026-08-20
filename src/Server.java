import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class Server {

    public static void main(String[] args) throws IOException {

        int port = Integer.parseInt(
        System.getenv().getOrDefault("PORT", "8080")
);

HttpServer server = HttpServer.create(
        new InetSocketAddress("0.0.0.0", port), 0
);

        // Test page
        server.createContext("/", exchange -> {

            String response = "Hotel Management System Backend is Running!";

            sendResponse(exchange, response);
        });

        // Add customer
        server.createContext("/addCustomer", exchange -> {

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                String data = new String(
                        exchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8
                );

                String[] values = data.split("&");

                String name = values[0].split("=")[1];
                String phone = values[1].split("=")[1];

                name = java.net.URLDecoder.decode(
                        name, StandardCharsets.UTF_8
                );

                phone = java.net.URLDecoder.decode(
                        phone, StandardCharsets.UTF_8
                );

                try {

                    Connection con = DBConnection.getConnection();

                    String sql =
                            "INSERT INTO customer (name, phone) VALUES (?, ?)";

                    PreparedStatement ps =
                            con.prepareStatement(sql);

                    ps.setString(1, name);
                    ps.setString(2, phone);

                    ps.executeUpdate();

                    con.close();

                    sendResponse(
                            exchange,
                            "Customer added successfully!"
                    );

                } catch (Exception e) {

                    e.printStackTrace();

                    sendResponse(
                            exchange,
                            "Error adding customer: " + e.getMessage()
                    );
                }

            } else {

                sendResponse(
                        exchange,
                        "Only POST requests are allowed."
                );
            }
        });

// Get all customers
server.createContext("/customers", exchange -> {

    if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {

        try {

            Connection con = DBConnection.getConnection();

            String sql = "SELECT customer_id, name, phone FROM customer";

            PreparedStatement ps = con.prepareStatement(sql);

            var rs = ps.executeQuery();

            StringBuilder json = new StringBuilder();

            json.append("[");

            boolean first = true;

            while (rs.next()) {

                if (!first) {
                    json.append(",");
                }

                json.append("{");
                json.append("\"id\":").append(rs.getInt("customer_id")).append(",");
                json.append("\"name\":\"")
                        .append(rs.getString("name"))
                        .append("\",");
                json.append("\"phone\":\"")
                        .append(rs.getString("phone"))
                        .append("\"");
                json.append("}");

                first = false;
            }

            json.append("]");

            con.close();

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "application/json"
            );

            sendResponse(exchange, json.toString());

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    "{\"error\":\"Could not load customers\"}"
            );
        }

    } else {

        sendResponse(
                exchange,
                "{\"error\":\"Only GET requests are allowed\"}"
        );
    }
});

// Get all rooms
server.createContext("/rooms", exchange -> {

    if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {

        try {

            Connection con = DBConnection.getConnection();

            String sql = "SELECT room_id, room_number, room_type, price, status FROM room";

            PreparedStatement ps = con.prepareStatement(sql);

            var rs = ps.executeQuery();

            StringBuilder json = new StringBuilder();

            json.append("[");

            boolean first = true;

            while (rs.next()) {

                if (!first) {
                    json.append(",");
                }

                json.append("{");

                json.append("\"id\":")
                        .append(rs.getInt("room_id"))
                        .append(",");

                json.append("\"number\":")
                        .append(rs.getInt("room_number"))
                        .append(",");

                json.append("\"type\":\"")
                        .append(rs.getString("room_type"))
                        .append("\",");

                json.append("\"price\":")
                        .append(rs.getDouble("price"))
                        .append(",");

                json.append("\"status\":\"")
                        .append(rs.getString("status"))
                        .append("\"");

                json.append("}");

                first = false;
            }

            json.append("]");

            con.close();

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "application/json"
            );

            sendResponse(exchange, json.toString());

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    "{\"error\":\"Could not load rooms\"}"
            );
        }

    } else {

        sendResponse(
                exchange,
                "{\"error\":\"Only GET requests are allowed\"}"
        );
    }
});

// Book a room
server.createContext("/bookRoom", exchange -> {

    if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

        try {

            String data = new String(
                    exchange.getRequestBody().readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8
            );

            String[] values = data.split("&");

            int customerId = Integer.parseInt(
                    values[0].split("=")[1]
            );

            int roomId = Integer.parseInt(
                    values[1].split("=")[1]
            );

            String checkIn = java.net.URLDecoder.decode(
                    values[2].split("=")[1],
                    java.nio.charset.StandardCharsets.UTF_8
            );

            Connection con = DBConnection.getConnection();

            // Check whether customer exists
            String customerSql =
                    "SELECT customer_id FROM customer WHERE customer_id = ?";

            PreparedStatement customerPs =
                    con.prepareStatement(customerSql);

            customerPs.setInt(1, customerId);

            var customerRs = customerPs.executeQuery();

            if (!customerRs.next()) {

                con.close();

                sendResponse(
                        exchange,
                        "Customer ID not found!"
                );

                return;
            }

            // Check room
            String roomSql =
                    "SELECT status FROM room WHERE room_id = ?";

            PreparedStatement roomPs =
                    con.prepareStatement(roomSql);

            roomPs.setInt(1, roomId);

            var roomRs = roomPs.executeQuery();

            if (!roomRs.next()) {

                con.close();

                sendResponse(
                        exchange,
                        "Room not found!"
                );

                return;
            }

            if (!roomRs.getString("status").equals("Available")) {

                con.close();

                sendResponse(
                        exchange,
                        "Room is already booked!"
                );

                return;
            }

            // Add booking
            String bookingSql =
                    "INSERT INTO booking " +
                    "(customer_id, room_id, check_in) " +
                    "VALUES (?, ?, ?)";

            PreparedStatement bookingPs =
                    con.prepareStatement(bookingSql);

            bookingPs.setInt(1, customerId);
            bookingPs.setInt(2, roomId);
            bookingPs.setDate(
                    3,
                    java.sql.Date.valueOf(checkIn)
            );

            bookingPs.executeUpdate();

            // Change room status
            String updateRoomSql =
                    "UPDATE room SET status = 'Booked' " +
                    "WHERE room_id = ?";

            PreparedStatement updateRoomPs =
                    con.prepareStatement(updateRoomSql);

            updateRoomPs.setInt(1, roomId);

            updateRoomPs.executeUpdate();

            con.close();

            sendResponse(
                    exchange,
                    "Room booked successfully!"
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    "Error booking room: " + e.getMessage()
            );
        }

    } else {

        sendResponse(
                exchange,
                "Only POST requests are allowed."
        );
    }
});

      // Check out customer
server.createContext("/checkout", exchange -> {

    if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

        try {

            String data = new String(
                    exchange.getRequestBody().readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8
            );

            String[] values = data.split("&");

            int bookingId = Integer.parseInt(
                    values[0].split("=")[1]
            );

            Connection con = DBConnection.getConnection();

            // Find the room for this booking
            String findSql =
                    "SELECT room_id FROM booking WHERE booking_id = ?";

            PreparedStatement findPs =
                    con.prepareStatement(findSql);

            findPs.setInt(1, bookingId);

            var rs = findPs.executeQuery();

            if (!rs.next()) {

                con.close();

                sendResponse(
                        exchange,
                        "Booking ID not found!"
                );

                return;
            }

            int roomId = rs.getInt("room_id");

            // Update checkout date
            String bookingSql =
                    "UPDATE booking " +
                    "SET check_out = CURDATE() " +
                    "WHERE booking_id = ?";

            PreparedStatement bookingPs =
                    con.prepareStatement(bookingSql);

            bookingPs.setInt(1, bookingId);

            bookingPs.executeUpdate();

            // Make room available again
            String roomSql =
                    "UPDATE room " +
                    "SET status = 'Available' " +
                    "WHERE room_id = ?";

            PreparedStatement roomPs =
                    con.prepareStatement(roomSql);

            roomPs.setInt(1, roomId);

            roomPs.executeUpdate();

            con.close();

            sendResponse(
                    exchange,
                    "Customer checked out successfully!"
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    "Error during checkout: " +
                    e.getMessage()
            );
        }

    } else {

        sendResponse(
                exchange,
                "Only POST requests are allowed."
        );
    }
}); 

// Get all bookings
server.createContext("/bookings", exchange -> {

    if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {

        try {

            Connection con = DBConnection.getConnection();

            String sql =
                    "SELECT b.booking_id, c.name, " +
                    "r.room_number, r.room_type, " +
                    "b.check_in, b.check_out " +
                    "FROM booking b " +
                    "JOIN customer c ON b.customer_id = c.customer_id " +
                    "JOIN room r ON b.room_id = r.room_id";

            PreparedStatement ps =
                    con.prepareStatement(sql);

            var rs = ps.executeQuery();

            StringBuilder json = new StringBuilder();

            json.append("[");

            boolean first = true;

            while (rs.next()) {

                if (!first) {
                    json.append(",");
                }

                json.append("{");

                json.append("\"id\":")
                        .append(rs.getInt("booking_id"))
                        .append(",");

                json.append("\"customer\":\"")
                        .append(rs.getString("name"))
                        .append("\",");

                json.append("\"room\":")
                        .append(rs.getInt("room_number"))
                        .append(",");

                json.append("\"type\":\"")
                        .append(rs.getString("room_type"))
                        .append("\",");

                json.append("\"checkIn\":\"")
                        .append(rs.getDate("check_in"))
                        .append("\",");

                json.append("\"checkOut\":");

                if (rs.getDate("check_out") == null) {
                    json.append("null");
                } else {
                    json.append("\"")
                            .append(rs.getDate("check_out"))
                            .append("\"");
                }

                json.append("}");

                first = false;
            }

            json.append("]");

            con.close();

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "application/json"
            );

            sendResponse(exchange, json.toString());

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    "{\"error\":\"Could not load bookings\"}"
            );
        }

    } else {

        sendResponse(
                exchange,
                "{\"error\":\"Only GET requests are allowed\"}"
        );
    }
});

server.start();

        System.out.println(
                "Server started at http://localhost:8080"
        );
    }


    // Send response to browser
    public static void sendResponse(
            HttpExchange exchange,
            String response
    ) throws IOException {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.sendResponseHeaders(
                200,
                response.getBytes(StandardCharsets.UTF_8).length
        );

        exchange.getResponseBody().write(
                response.getBytes(StandardCharsets.UTF_8)
        );

        exchange.close();
    }
}