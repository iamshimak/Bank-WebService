import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by ShimaK on 05-Mar-17.
 */
public class EmployeeService {
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

    private InetAddress ipAddress = null;
    private String hostname = null;
    private int portNumber = 10007;
    private ServerSocket serverSocket = null;
    private Socket clientSocket = null;

    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;

    public static void main(String[] args) throws IOException {
        EmployeeService server = new EmployeeService();
        server.startup();
        server.connect();
        server.receiveAndSend();
        server.disconnect();
    }

    private void startup() throws IOException {
        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

        System.out.println("[EMPLOYEE SERVICE][" + dateFormat.format(new Date()) + "] - Initializing Simple Server");
        ipAddress = InetAddress.getLocalHost();
        hostname = InetAddress.getLocalHost().getHostName();

        //serverSocket = new ServerSocket(portNumber, 0, InetAddress.getByName("127.0.0.1"));
        serverSocket = new ServerSocket(portNumber);
        System.out.println("[SERVER SOCKET][" + serverSocket.getLocalSocketAddress() + "]");
    }

    private void connect() throws IOException {
        clientSocket = serverSocket.accept();
    }

    private void receiveAndSend() throws IOException {
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());

        HashMap<String, String> request, response;

        try {
            request = (HashMap<String, String>) in.readObject();
        } catch (ClassNotFoundException e) {
            System.out.println("[CLASS NOT FOUND]");
            return;
        }

        response = new HashMap<>();

        switch (request.get("SERVICE")) {
            case "CRUD":
                try {
                    manipulateEmployee(request);
                    response.put("RESULT", "SUCCESS");
                    System.out.println("[RESPONSE CREATED] [" + dateFormat.format(new Date()) + "]");
                } catch (SQLException e) {
                    e.printStackTrace();
                    response.put("RESULT", "FAILED");
                    response.put("ERROR", e.getErrorCode() + "");
                }
                break;
            case "LOGIN":
                switch (loginEmployee(request)) {
                    case -1:
                        System.out.println("[ACCOUNT NOT EXIST]");
                        response.put("RESULT", "FAILED");
                        response.put("ERROR", "USERNAME_NOT_VALID");
                        break;
                    case 0:
                        System.out.println("[LOGIN FAILED]");
                        response.put("RESULT", "FAILED");
                        response.put("ERROR", "PASSWORD_NOT_MATCH");
                        break;
                    case 1:
                        System.out.println("[LOGGED IN]");
                        response.put("RESULT", "SUCCESS");
                        break;
                }
                break;
        }
        out.writeObject(response);
        System.out.println("[RESPONSE SEND] [" + dateFormat.format(new Date()) + "]");
    }

    private boolean manipulateEmployee(HashMap<String, String> input) throws SQLException {
        Connection conn = null;
        Statement statement;
        //TODO catch NullPoint Exception for hashMap values
        try {
            conn = connectDB();
            statement = conn.createStatement();

            String SQL = null;
            switch (input.get("SERVICE_TYPE")) {
                case "CREATE":
                    SQL = "INSERT INTO employee VALUES ('" + input.get("NAME") + "','" + input.get("POSITION") + "','"
                            + input.get("USERNAME") + "','" + input.get("PASSWORD") + "')";
                    break;
                case "UPDATE":
                    //TODO develop update add another statement to hashmap object (identify update field)
                    String column = null;
                    switch (input.get("UPDATE_TYPE")) {
                        case "NAME":
                            column = "name";
                            break;
                        case "POSITION":
                            column = "position";
                            break;
                        case "PASSWORD":
                            column = "password";
                            break;
                        case "USERNAME":
                            column = "username";
                            break;
                        default:
                            //TODO throw exception
                    }

                    SQL = "UPDATE employee SET " + column + " = '" + input.get("UPDATE_VALUE")
                            + "' WHERE username = '" + input.get("USERNAME") + "'";
                    break;
                case "DELETE":
                    SQL = "DELETE FROM employee WHERE username='" + input.get("USERNAME") + "'";
                    break;
                default:
                    //TODO throw exception
            }

            statement.executeUpdate(SQL);
            return true;

        } finally {
            conn.close();
        }
    }

    private int loginEmployee(HashMap<String, String> input) {
        Connection conn = null;
        Statement statement;
        ResultSet result;

        try {
            conn = connectDB();
            statement = conn.createStatement();

            String SQL = "SELECT password FROM employee WHERE username = '" + input.get("USERNAME") + "'";

            result = statement.executeQuery(SQL);

            if (result.next()) {
                System.out.println("[DB PASSWORD] " + result.getString(1) + " [USER PASSWORD] " + input.get("PASSWORD"));
                if (result.getString("password").equals(input.get("PASSWORD"))) {
                    return 1;
                }
                return 0;
            }
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                return -1;
            }
        }
    }

    private Connection connectDB() throws SQLException {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost/bankdb", "root", "");
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    private void disconnect() throws IOException {
        System.out.println("[EMPLOYEE SERVICE][" + ipAddress + ":" + portNumber + "][" + dateFormat.format(new Date()) + "] - Server disconnecting....");
        out.close();
        in.close();
        clientSocket.close();
        serverSocket.close();
        System.out.println("[EMPLOYEE SERVICE][" + ipAddress + ":" + portNumber + "][" + dateFormat.format(new Date()) + "] - .... server disconnected");
    }
}
