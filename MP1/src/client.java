import java.io.*;
import java.net.*;
import java.util.Scanner;

class client{
	public static void main (String argv[]) throws Exception
	{
		String request;
		String response;
		int serverPort = 6789;
		String EOL = System.lineSeparator();

		//creating client socket
		Socket clientSocket = new Socket("localhost",serverPort);

		//creating streams for stdin, to output characters to the server, and to receive from the socket
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		//SENDING TO SERVER
		request = inFromUser.readLine();
		String[] requestParts = request.split(" ");
		if(requestParts.length == 1)
			outToServer.writeBytes(HTTPGet(requestParts[0], null));
			
		else if(requestParts.length == 2)
			outToServer.writeBytes(HTTPGet(requestParts[0], requestParts[1]));
		else {
			System.out.print("ERROR: Request format is not suported");
			System.exit(0);
		}
		
		//RECIEVING FROM SERVER:
		
		//First Line
		response = inFromServer.readLine();
		String[] fstLine = response.split(" ");
		System.out.println("Server response:");
		System.out.println(" " + "Protocol/Version: " + fstLine[0]);
		System.out.println(" " + "Code: " + fstLine[1]);
		System.out.println(" " + "Phrase: " + fstLine[2]);
		
		//Headers
		if(inFromServer.ready()) {
			System.out.println("Headers: ");
			while(!((response = inFromServer.readLine()).equals(""))) {
				System.out.println(" " + response);
				if(response.split(" ")[0].equals("Set-cookie:")) {
					PrintWriter cookieWriter = new PrintWriter("Client/cookie.txt");
					cookieWriter.write((response.split(" ")[1]));
					cookieWriter.close();
				}
			}
		}
		
		//Data
		if(fstLine[1].equals("200")) {
		PrintWriter fileWriter = new PrintWriter("Client/" + request.split(" ")[0]);
		fileWriter.print(inFromServer.readLine());
		while(inFromServer.ready())
			fileWriter.print(EOL + inFromServer.readLine());
		fileWriter.close();
		}

		//closing I/O streams and socket
		inFromUser.close();
		inFromServer.close();
		outToServer.close();
		clientSocket.close();
	}
	
	
	public static String HTTPGet (String URL, String lastMod) {
		String EOL = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		//First Line
		sb.append("GET " + URL + " 1.1" + EOL);
		//Cookie
		try {
			Scanner sc = new Scanner(new File("Client/cookie.txt"));
			sb.append("Cookie: " + sc.nextLine() + EOL);
		}
		catch(Exception e){}
		//Last Modification
		if(lastMod != null) {
			sb.append("Last-Modified: " + lastMod + EOL);
		}
		//Signalling end of request
		sb.append(EOL);
		return sb.toString();
	}
}
