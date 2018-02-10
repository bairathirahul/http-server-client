import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;


public class HTTPServer {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Error! Enter Server Port Number");
            System.exit(1);
        }

        System.out.println("Server Started");

        int portNo = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(portNo);
        Socket clientSocket = null;
        System.out.println("Server Waiting for client");
        while (true) {
            if (serverSocket.getLocalPort() == -1) {
                break;
            }
            clientSocket = serverSocket.accept();
            System.out.println("Waiting on port " + serverSocket.getLocalPort() + " for the client");
            HTTPServerThread ft = new HTTPServerThread(clientSocket);
            ft.start();
        }
    }
}

class HTTPServerThread extends Thread {
    private Socket clientSocket;

    public HTTPServerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        DataInputStream inputStream = null;
        DataOutputStream outputStream = null;
        try {
            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // Prepare request
            HTTPRequest request = new HTTPRequest(inputStream);

            // Generate response
            HTTPResponse response = request.process();
            response.setOutputStream(outputStream);
            response.process();
        } catch (HTTPException e) {
            e.printStackTrace();
            HTTPResponse errorResponse = new HTTPResponse();
            errorResponse.setOutputStream(outputStream);
            errorResponse.setStatus(e.code);
            errorResponse.setVersion("HTTP/1.1");
            errorResponse.process();
        }

        // Cleanup resources
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class HTTPRequest {
        private static final String METHOD_GET = "GET";
        private static final String METHOD_PUT = "PUT";
        private static final String HTTP_STATUS_OK = "200 OK";

        private String method;
        private String object;
        private String version;
        private HashMap<String, String> headers = new HashMap<String, String>();
        private DataInputStream inputStream;

        public HTTPRequest(DataInputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void parseRequest(String line) throws HTTPException {
            StringTokenizer tokenizer = new StringTokenizer(line, " ");
            this.method = tokenizer.nextToken();
            this.object = tokenizer.nextToken();

            // Remove leading slash
            if (this.object.startsWith("/")) {
                this.object = this.object.substring(1);
            }

            this.version = tokenizer.nextToken();

            if (!this.version.equals("HTTP/1.1")) {
                throw new HTTPException(HTTPException.HTTP_VERSION_NOT_SUPPORTED);
            }
        }

        public void parseHeader(String line) {
            String[] pairs = line.split(": ");
            if (pairs.length == 2) {
                this.headers.put(pairs[0].trim(), pairs[1].trim());
            }
        }

        public HTTPResponse process() throws HTTPException {
            // Read request and headers
            try {
                this.parseRequest(inputStream.readLine());
                String line;
                while (!(line = inputStream.readLine()).equals("")) {
                    this.parseHeader(line);
                }
            } catch (IOException e) {
                throw new HTTPException(HTTPException.HTTP_INTERNAL_SERVER);
            }

            switch (this.method) {
                case HTTPRequest.METHOD_GET:
                    return this.get();
                case HTTPRequest.METHOD_PUT:
                    return this.put();
                default:
                    throw new HTTPException(HTTPException.HTTP_METHOD_NOT_ALLOWED);
            }
        }

        private HTTPResponse get() throws HTTPException {
            File file = new File(this.object);
            if (!file.isFile()) {
                throw new HTTPException(HTTPException.HTTP_NOT_FOUND);
            }

            // Prepare response
            HTTPResponse response = new HTTPResponse();
            response.setVersion(this.version);
            response.setStatus(HTTP_STATUS_OK);

            // Set server
            response.getHeaders().put("Server", "Custom server written in Java");

            // Set Content Type and Length
            response.getHeaders().put("Content-Type", URLConnection.guessContentTypeFromName(file.getName()));
            response.getHeaders().put("Content-Length", String.valueOf(file.length()));

            // Set current date
            SimpleDateFormat formatter = new SimpleDateFormat();
            response.getHeaders().put("Date", formatter.format(new Date()));

            response.setObject(this.object);
            return response;
        }

        private HTTPResponse put() throws HTTPException {
            int contentLength = 0;
            if (!this.headers.containsKey("Content-Length")) {
                throw new HTTPException(HTTPException.HTTP_BAD_REQUEST);
            }

            try {
                contentLength = Integer.parseInt(this.headers.get("Content-Length"));
            } catch (NumberFormatException e) {
                throw new HTTPException(HTTPException.HTTP_BAD_REQUEST);
            }

            File file = new File(this.object + '1');
            FileOutputStream fileOutputStream = null;
            try {
                if (!file.isFile()) {
                    file.createNewFile();
                }
                fileOutputStream = new FileOutputStream(file);

                int readLength;
                byte[] buffer = new byte[1024];
                while (contentLength > 0) {
                    readLength = this.inputStream.read(buffer);
                    fileOutputStream.write(buffer, 0, readLength);
                    contentLength -= 1024;
                }
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new HTTPException(HTTPException.HTTP_INTERNAL_SERVER);
            }

            // Prepare response
            HTTPResponse response = new HTTPResponse();
            response.setVersion(this.version);
            response.setStatus(HTTP_STATUS_OK);

            // Set server
            response.getHeaders().put("Server", "Custom server written in Java");

            // Set current date
            SimpleDateFormat formatter = new SimpleDateFormat();
            response.getHeaders().put("Date", formatter.format(new Date()));

            return response;
        }
    }


    private class HTTPResponse {
        private String version;
        private String status;
        private HashMap<String, String> headers = new HashMap<String, String>();
        private DataOutputStream outputStream;
        private String object;

        public void setOutputStream(DataOutputStream outputStream) {
            this.outputStream = outputStream;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public HashMap<String, String> getHeaders() {
            return headers;
        }

        public void setObject(String object) {
            this.object = object;
        }

        public void process() {
            try {
                // Writer response
                String response = this.version + " " + this.status + "\r\n";
                this.outputStream.write(response.getBytes());

                // Write headers
                for (String key : this.headers.keySet()) {
                    String header = key + ": " + this.headers.get(key) + "\r\n";
                    this.outputStream.write(header.getBytes());
                }

                // Write empty line
                String empty = "\r\n";
                this.outputStream.write(empty.getBytes());

                // Write output content
                if (this.object != null) {
                    FileInputStream fileInputStream = new FileInputStream(this.object);
                    int readLength;
                    byte[] buffer = new byte[1024];
                    while ((readLength = fileInputStream.read(buffer)) != -1) {
                        this.outputStream.write(buffer, 0, readLength);
                    }
                    fileInputStream.close();
                }
                this.outputStream.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class HTTPException extends Throwable {
        public static final String HTTP_BAD_REQUEST = "400 Bad Request";
        public static final String HTTP_NOT_FOUND = "404 Not Found";
        public static final String HTTP_METHOD_NOT_ALLOWED = "405 Method Not Allowed";
        public static final String HTTP_INTERNAL_SERVER = "500 Internal Server Error";
        public static final String HTTP_VERSION_NOT_SUPPORTED = "505 HTTP Version Not Supported";

        private String code;

        public HTTPException(String code) {
            this.code = code;
        }
    }
}