import java.io.*;
import java.util.*;

public class TrafficAnalysis {
	public static void main (String argv[]) throws Exception
	{

		//Reading CSV
		File file = new File("MP2_Grupo33.csv");
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);

		String line;
		int counter = 0;
		int IPv4Counter = 0;
		int IPv6Counter = 0;
		HashSet<String> IPv4DestHS = new HashSet<String>();
		HashSet<String> TCPHS = new HashSet<String>();
		long totalPacketSize = 0;
		int maxPacketSize = 0;
		int minPacketSize = 0;
		int TCPFailures = 0;
		List<Double> ccdfValues = new ArrayList<Double>();

		while ((line = br.readLine()) != null) {
			counter++;
			if (counter == 1) continue; //ignore first line
			if (counter == 16989400) break; //Stopping before broken line

			StringTokenizer token = new StringTokenizer(line,",");

			token.nextToken(); 													//No.
			token.nextToken(); 													//Time
			String sourceAddress = token.nextToken().replace("\"", ""); 		//Source IP
			String destinationAddress = token.nextToken().replace("\"", ""); 	//Destination IP
			String sourcePort = token.nextToken().replace("\"", ""); 			//Source Port
			token.nextToken(); 													//Destination Port
			String protocol = token.nextToken().replace("\"", ""); 				//Protocol
			String lengthSt = token.nextToken().replace("\"", ""); 				//Length
			String flags = token.nextToken().replace("\"", ""); 				//Flags

			if(sourceAddress.contains(".")) {
				IPv4Counter++;
				IPv4DestHS.add(destinationAddress);
			}
			else
				IPv6Counter++;

			if(protocol.equals("TCP")) {
				TCPHS.add(sourcePort);
				
				
				if(flags.length() > 0 && flags.subSequence(2, 4).equals("00")) {
					int lastFlagValue = flags.toUpperCase().charAt(4) - '0';
					if((lastFlagValue >= 4 && lastFlagValue <= 7) || (lastFlagValue >= 12 && lastFlagValue <= 15))
						TCPFailures++;
				}
			}

			int length = Integer.parseInt(lengthSt);
			totalPacketSize += length;

			if (length > maxPacketSize)
				maxPacketSize = length;

			if (length < minPacketSize || minPacketSize == 0)
				minPacketSize = length;
			
			int packetSizeCounter = 1;
			while(packetSizeCounter <= length) {
				if(packetSizeCounter >= ccdfValues.size())
					ccdfValues.add(1.0); // Increasing the size of the List, for when a new max value is reached
				else
					ccdfValues.set(packetSizeCounter, ccdfValues.get(packetSizeCounter) + 1.0); // Increasing the value in the List by 1
				packetSizeCounter++;
			}
			
			if (counter % 100000 == 0)
				System.out.println(counter + " lines have been processed.");
		}
		counter -= 2; //accounting for the counter being ahead of the package count by one, plus the skipped last line
		br.close();
		System.out.println("Q1 Ammount of IPv4 Packets: " + IPv4Counter);
		System.out.println("Q2 Ammount of IPv6 Packets: " + IPv6Counter);
		System.out.println("Q3 Ammount of unique IPv4 Hosts: " + IPv4DestHS.size());
		System.out.println("Q4 Ammount of unique Client Port using TCP: " + TCPHS.size());
		System.out.println("Q5 Average Packet size: " + (double)Math.round((double)totalPacketSize/counter * 100)/100);
		System.out.println("Q5 Maximum Packet size: " + maxPacketSize);
		System.out.println("Q5 Minimum Packet size: " + minPacketSize);
		System.out.println("Q6 Ammount of TCP failures: " + TCPFailures);
		
		for(int i = 1; i < ccdfValues.size(); i++) {
			ccdfValues.set(i, ccdfValues.get(i)/counter); //Turns the values in the list into fractions for CCDF
		}

		//create plot with list of numbers
		//first, create data file
		BufferedWriter myDataFile = new BufferedWriter(new FileWriter("myData.txt"));
		int i = 1;
		String EOL = System.lineSeparator();
		for (Iterator<Double> ccdfItr = ccdfValues.iterator(); ccdfItr.hasNext();) {
			myDataFile.write(String.format("%d %.2f", i++, ccdfItr.next()) + EOL);
		}

		//second, create gnuplot file
		BufferedWriter myGnuplotFile = new BufferedWriter(new FileWriter("myPlot.gp"));
		myGnuplotFile.write("set terminal svg size 350,262\n");
		myGnuplotFile.write("set output 'plot.svg'\n");
		myGnuplotFile.write("set xrange [0:" + maxPacketSize + "]\n");
		myGnuplotFile.write("set yrange [0:1]\n");
		myGnuplotFile.write("plot 'myData.txt'\n");

		myDataFile.close();
		myGnuplotFile.close();
	}
}
