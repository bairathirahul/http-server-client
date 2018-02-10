import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class HTTPClient {
	public static void main(String[] args) throws IOException
	{
		Socket clientSocket = null;
		FileInputStream inputFile = null;
		if(args.length<1)
		{
			System.err.println("Enter Server Port Number");
			System.exit(1);
		}
		String hostName = args[0];
		int port = Integer.parseInt(args[1]);
		String command = args[2];
		String fileName = args[3];
		System.out.println("Host Name: "+args[0]);
		System.out.println("Port Number:  "+args[1]);
		System.out.println("Command: "+args[2]);
		System.out.println("File Name: "+args[3]);
		int i=0;
		ArrayList<String> listLines = new ArrayList<String>();
		try {
			clientSocket = new Socket(hostName, port);
			BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
			String message = "";
			
			if(command.equals("GET"))
			{
				pw.println("GET" + fileName + " HTTP/1.1");
				pw.println("Host: "+hostName);
				pw.println("Connection status: open");
			
				while(true)
				{
					message = br.readLine();
					if(message == null)
						break;
					listLines.add(message);
					System.out.println(message);
				}
			}
			else if(command.equals("PUT"))
			{
				if(new File(fileName).isFile())
				{
					pw.println("PUT " + fileName + " HTTP/1.1");
					pw.println("Host: " + hostName);
					pw.println("Connection Status: open");
					
					System.out.println("File is: " + fileName);
					File fileInput = new File(fileName);
					inputFile = new FileInputStream(fileInput);
					
					do {
						i = inputFile.read();
						if(i != -1)
						{
							message += Character.toString((char) i);
							pw.println(i);
						}
					}while(i != -1);
						if(i == -1)
						{
							pw.println("-1");
						}
						System.out.println(br.readLine());
				}
				else
				{
					System.out.println("File doesn't exists, please enter a valid file");
				}
			}
		}catch(IOException e)
		{
			System.out.println(e);
		}
		finally
		{
			if(clientSocket != null)
			{
				clientSocket.close();
			}
		}
		
	}
	
	
	
}
