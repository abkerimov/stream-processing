package org.example;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;

public class Server{
    public static void server() {
        int port = 8080; // Port for the server
        String logFile = "./logs/server.log"; // Log file

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    logRequest(clientSocket, logFile);

                    handleRequest(clientSocket, logFile);
                } catch (IOException e) {
                    System.err.println("Error handling client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    private static void handleRequest(Socket clientSocket, String logFile) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            return;
        }
        logMessage("Request: " + requestLine, logFile);

        String[] parts = requestLine.split(" ");
        String method = parts[0];
        String path = parts.length > 1 ? parts[1] : "/";

        String response;
        if (!method.equals("GET")) {
            response = "HTTP/1.1 405 Method Not Allowed\r\nContent-Type: text/plain\r\n\r\nMethod Not Allowed";
        } else if ("/hello".equals(path)) {
            response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nHello, World!";
        } else if ("/time".equals(path)) {
            response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n" + LocalDateTime.now();
        } else if ("/status".equals(path)) {
            response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nServer is running.";
        } else if ("/logs".equals(path)) {
            response = readLogFile(logFile);
        } else {
            response = "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nNot Found";
        }

        out.write(response);
        out.flush();
    }

    private static void logRequest(Socket clientSocket, String logFile) {
        try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFile, true))) {
            String logEntry = String.format("[%s] %s:%d connected\n",
                    LocalDateTime.now(),
                    clientSocket.getInetAddress(),
                    clientSocket.getPort());
            logWriter.write(logEntry);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    private static void logMessage(String message, String logFile) {
        try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFile, true))) {
            String logEntry = String.format("[%s] %s\n", LocalDateTime.now(), message);
            logWriter.write(logEntry);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    private static String readLogFile(String logFile) {
        StringBuilder content = new StringBuilder("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n");
        try (BufferedReader logReader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = logReader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            content.append("Error reading log file: ").append(e.getMessage());
        }
        return content.toString();
    }
}

