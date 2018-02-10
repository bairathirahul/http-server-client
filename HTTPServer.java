import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.SocketTimeoutException;
import java.net.Socket;
import java.net.ServerSocket;

public class HTTPServer extends Thread {
    private ServerSocket serverSocket;

    private HTTPServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void run() {
        while (true) {
            Socket socket = null;
            DataInputStream dis = null;
            DataOutputStream dos = null;
            try {
                socket = serverSocket.accept();
                dis = new DataInputStream(socket.getInputStream());

                // Todo: Parse the incoming packet

                dos = new DataOutputStream(socket.getOutputStream());
                // Write some output
                //dos.write("");
            } catch (SocketTimeoutException e) {
                // Todo: Generate socket timeout exception
            } catch (IOException e) {
                // Implement IO Exception
            } finally {
                try {
                    // Release resources
                    if (dis != null) {
                        dis.close();
                    }
                    if (dos != null) {
                        dos.close();
                    }
                    if (socket != null) {
                        socket.close();
                    }
                } catch (Exception e) {
                    // Do nothing
                }
            }
        }
    }

    public static void main(String[] args) {
        int port = 1025;
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid port number provided");
            }
        }

        HTTPServer server = null;
        try {
            server = new HTTPServer(port);
            server.run();
        } catch (IOException e) {
            System.out.println("Server cannot be initiated. See below for error details");
            e.printStackTrace();
        }
    }
}