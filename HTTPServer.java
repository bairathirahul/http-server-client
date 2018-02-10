import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;



public class HTTPServer{
	  public static void main(String[] args) throws IOException
	  {
		 if(args.length<1)
		 {
		  	System.err.println("Enter Server Port Number:");
		  	System.exit(1);
		 }
		 System.out.println("Server Started");
		 int portNo = Integer.parseInt(args[0]);
		 ServerSocket serverSocket = new ServerSocket(portNo);
		 Socket clientsocket = null;
		 System.out.println ("Server Waiting for client");
		 while(true) {
	 		if(serverSocket.getLocalPort()==-1)
	 		{
	 			break;
	 		}
	 	    clientsocket = serverSocket.accept();
	 	    System.out.println("Waiting on port " + serverSocket.getLocalPort() + " for the client");
	 	   	HTTPServerThread ft= new HTTPServerThread(clientsocket);
	 		ft.start();
	 	 }
		
	  }
}
class HTTPServerThread extends Thread{
	int i=0;
	String message="";
	Socket clientsocket;
	
	public HTTPServerThread(Socket clientsocket)
	{
		this.clientsocket = clientsocket;
	}

	public void run()
	{			
		PrintWriter pw=  null;
		try 
		{
			BufferedReader br;
			String inputLine = null;
			FileInputStream fileInput = null;
			FileOutputStream fileOutput = null;
			System.out.println("Connection Established with Client" + clientsocket.getInetAddress() + " Port:" + clientsocket.getPort());
		    pw = new PrintWriter(clientsocket.getOutputStream(), true);
			br = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
			
			if((inputLine = br.readLine()) != null)
			{             	
				String input = inputLine;
				StringTokenizer tokenizer = new StringTokenizer(input);
				String httpMethod = tokenizer.nextToken();
				String httpFileName = tokenizer.nextToken();
			    String hostName = br.readLine();
				String connStatus = br.readLine();
		       
				if(httpMethod.equals("GET"))
				{
					String fileName = httpFileName;
					System.out.println("Client Says: Get  "+httpFileName);

					if(new File(fileName).isFile())
					{
						fileInput = new FileInputStream(fileName);
						byte[] buffer = new byte[1024] ;
				     	int bytesRead;
						do
						{
							i = fileInput.read();
							if(i != -1)
							message = message+Character.toString((char) i);
						}while(i != -1);
	
						System.out.println("Content in "+fileName+" displayed to client:\n"+message);
						pw.println("HTTP/1.1 200 OK");
						pw.println("Server: Java HTTPServer");
						pw.println("Content-length:" +message.length()+ "\r\n");	
						pw.println(message);	
					}
					else
					{
						pw.println("404 File Not Found");
					}
				}
			
			if(httpMethod.equals("PUT"))
			{
				System.out.println("Client Says: PUT  "+httpFileName);
				File fileSave = new File(httpFileName);
				if (!fileSave.exists())
				{
					fileSave.createNewFile();
					System.out.println("Saving File: "+fileSave);
					fileOutput = new FileOutputStream(fileSave);
					i= Integer.parseInt(br.readLine());
					while(i!=-1)
					{
						System.out.println(i);
						fileOutput.write(i);
						i= Integer.parseInt(br.readLine());
					}
					fileOutput.close();
					pw.println("Saved File successfully");
				}
				else
				{
					System.out.println("File Already exists");
					pw.println("File Already exists");
					
				}
			
			}
			
		}  	
		
   	}	
	catch (IOException e) 
	{
		e.printStackTrace();
	}
	finally
	{
		try 
		{
		pw.close();
			if(clientsocket!=null)
			{
				clientsocket.close();
			}
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
		catch(NullPointerException n)
		{
			System.out.println(n);
		}
	}  
  }
}	

