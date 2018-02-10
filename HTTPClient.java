import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class HTTPClient {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Enter Server Port Number");
            System.exit(1);
        }

        String hostName = args[0];
        int port = Integer.parseInt(args[1]);
        String command = args[2];
        String fileName = args[3];

        Socket clientSocket = null;
        DataOutputStream outputStream = null;
        BufferedReader bufferedReader = null;
        try {
            clientSocket = new Socket(hostName, port);
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Write request
            String request = command + " " + fileName + " HTTP/1.1\r\n";
            outputStream.write(request.getBytes());

            if (command.equals("PUT")) {
                long fileLength = (new File(fileName)).length();
                String header = "Content-Length: " + fileLength + "\r\n";
                outputStream.write(header.getBytes());

                String spacer = "\r\n";
                outputStream.write(spacer.getBytes());

                FileInputStream fileInputStream = new FileInputStream(fileName);
                int readLength;
                byte[] buffer = new byte[1024];
                while ((readLength = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, readLength);
                }
            } else {
                String spacer = "\r\n";
                outputStream.write(spacer.getBytes());
            }
            outputStream.flush();

            int readLength;
            char[] buffer = new char[1024];
            while ((readLength = bufferedReader.read(buffer)) != -1) {
                System.out.println(Arrays.copyOfRange(buffer, 0, readLength));
            }
        } catch (FileNotFoundException e) {
            System.out.println("File " + fileName + " not found");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Unexpected error. See the stack trace below for details");
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}