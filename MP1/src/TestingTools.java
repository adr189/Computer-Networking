import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

class TestingTools {

	public static void main(String[] args) throws IOException {
		BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.print(getLastMod(new File(userInput.readLine())));
	}
	
	public static Long getLastMod(File file) {
		return file.lastModified();
	}
}
