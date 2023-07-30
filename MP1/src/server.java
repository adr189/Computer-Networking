import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Random;
import java.util.Scanner;

class server{
	public static void main (String argv[]) throws Exception
	{
		int portNumber = 6789;
		String EOL = System.lineSeparator();

		//creating welcome socket (the door to knock) -- only used to accept connections
		ServerSocket welcomeSocket = new ServerSocket(portNumber);

		while(true){
			//creating connection socket -- one per TCP connection
			Socket connectionSocket = welcomeSocket.accept();

			//creating input and output streams (to receive from/send to client socket)
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

			//RECEIVING FROM CLIENT
			String request = inFromClient.readLine();
			String[] fstLine = request.split(" ");
			if(fstLine.length == 3) {
				System.out.println("Client request:");
				System.out.println(" " + "Method: " + fstLine[0]);
				System.out.println(" " + "URL: " + fstLine[1]);
				System.out.println(" " + "HTTP Version: " + fstLine[2]);
				if(fstLine[0].equals("GET")) {
					if(fstLine[2].equals("1.1")) {
						try {
							Scanner fileScanner = new Scanner(new File("Server/" + fstLine[1]));
							fileScanner.useDelimiter("//Z*");
							String code = "200";
							String phrase = "OK";
							//Headers
							if(inFromClient.ready()) {
								System.out.println("Headers: ");
								while(!((request = inFromClient.readLine()).equals(""))) {
									System.out.println(" " + request);
									if(request.split(" ")[0].equals("Last-Modified:")) {
										if(Long.parseLong(request.split(" ")[1]) == new File("Server/" + fstLine[1]).lastModified()) {
											fileScanner = null;
											code = "304";
											phrase = "Not_Modified";
										}
									}
								}
								outToClient.writeBytes(HTTPAnswer(code, phrase, fileScanner));
								System.out.println("Answer was sent to the Client. Code: " + code + ". Phrase: " + phrase + ".");
							}
						} catch (FileNotFoundException e) {
							outToClient.writeBytes("HTTP/1.1 404 Not_Found" + EOL);
							System.out.println("EROR: 404 Not_Found Sent to Client");
						}
					} else {outToClient.writeBytes("HTTP/1.1 505 HTTP_Version_Not_Supported" + EOL); System.out.println("EROR: 505 HTTP_Version_Not_Supported Sent to Client");}
				} else {outToClient.writeBytes("HTTP/1.1 501 Method_Unimplemented" + EOL); System.out.println("EROR: 501 Method_Unimplemented Sent to Client");}
			} else {outToClient.writeBytes("HTTP/1.1 400 Bad_Request" + EOL); System.out.println("EROR: 400 Bad_Request Sent to Client");}
			
			
			System.out.print(EOL + "-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-" + EOL + EOL);
			
			
			//closing the I/O streams and the socket
			connectionSocket.close();
			inFromClient.close();
			outToClient.close();
		}
	}
	
	public static String HTTPAnswer (String code, String phrase, Scanner fileScanner) {
		String EOL = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		Random rand = new Random();
		sb.append("1.1 " + code + " " + phrase + EOL);
		
		sb.append("Set-cookie: " + Math.abs(rand.nextLong()) + EOL);
		
		sb.append(EOL); //Blank space separating headers from data
		if(fileScanner != null)
			while(fileScanner.hasNext())
				sb.append(fileScanner.next() + EOL); //Data
		
		return sb.toString();
	}
	
}
