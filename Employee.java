import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by ShimaK on 06-Mar-17.
 */
public class Employee {
    public static void main(String[] args) throws IOException {
        /*
         This is to print the date and time
         */
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        /*
         These variables are to print the details of client and server
         */
        InetAddress ipAddress = null;
        String hostname = null;
        String message = null;

        String serverHostname = new String("127.0.0.1");
        int portNumber = 10007;

        /*
         This is to print the details of client and server
         */
        System.out.println("[CLIENT][" + dateFormat.format(new Date()) + "] - Initializing Simple Client");
        ipAddress = InetAddress.getLocalHost();
        hostname = InetAddress.getLocalHost().getHostName();

        System.out.println("[CLIENT][" + ipAddress + "][" + dateFormat.format(new Date()) + "] - Attempting to connect to host " + serverHostname + " on port " + portNumber);

        Socket echoSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            // echoSocket = new Socket("taranis", 7);
            echoSocket = new Socket(serverHostname, portNumber);
            out = new ObjectOutputStream(echoSocket.getOutputStream());
            in = new ObjectInputStream(echoSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("[CLIENT][" + ipAddress + "][" + dateFormat.format(new Date()) + "] - Don't know about host: " + serverHostname + ":" + portNumber);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("[CLIENT][" + ipAddress + "][" + dateFormat.format(new Date()) + "] - Couldn't get I/O for the connection to: " + serverHostname + ":" + portNumber);
            e.printStackTrace();
            System.exit(1);
        }

        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        HashMap<String, String> request = new HashMap<>();
        request.put("SERVICE", "CRUD");
        request.put("SERVICE_TYPE", "CREATE");
        request.put("UPDATE_TYPE", "NAME");
        request.put("UPDATE_VALUE", "Ahamed");
        request.put("NAME", "Tony");
        request.put("POSITION", "CEO");
        request.put("USERNAME", "irony");
        request.put("PASSWORD", "man");

        HashMap<String, String> response = new HashMap<>();

        out.writeObject(request);

        try {
            response = (HashMap<String, String>) in.readObject();
            System.out.println("[CLIENT][" + dateFormat.format(new Date()) + "]");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println(response.get("RESULT"));

        out.close();
        in.close();
        stdIn.close();
        echoSocket.close();
    }
}