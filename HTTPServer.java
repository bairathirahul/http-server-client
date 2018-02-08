public class HTTPServer extends Thread {
	ServerSocket serverSocket;
	public HTTPServer(int port, int timeout) {
		serverSocket = new ServerSocket(port);
		serverSocket.setSOTimeout(timeout);
	}

	public void run() {
		while(true) {
			Socket socket;
			DataInputStream dis;
			DataOutputStream dos;
			try {
				Socket socket = serverSocket.accpet();
				DataInputStream dis = new DataInputStream(socket);

				// Todo: Parse the incoming packet

				DataOutputStream dos = new DataOutputStream(socket);
				// Write some output
				out.write("")
			} catch(SocketTimeoutException e) {
				// Todo: Generate socket timeout exception
			} catch(IOException e) {
				// Implement IO Exception
			} finally {
				try {
					// Release resources
					if(dis != null) {
						dis.close();
					}
					if(dos != null) {
						dos.close();
					}
					if(socket != null) {
						socket.close();
					}
				} catch(Exception e) {
					// Do nothing
				}
			}
		}
	}

	
	public static void main(String[] args) {
		int port = 1025;
		if(args.length > 1) {
			try {
				port = Integer.parseInt(args[1]);
			} catch(NumberFormatException ex) {
				// Todo: Generate invalid port error

			}
		}

		int timeout = 1000;
		if(args.length > 2) {
			try {
				timeout = Integer.parseInt(args[2]);
			} catch(NumberFormatException ex) {
				// Todo: Generate invalid port error

			}
		}

		ServerSocket socket;
		try{
			socket = new ServerSocket(port);
		} catch(IOException e) {
			System.out.println("Server terminated");
			e.printStackTrace();
		}
	}
}