import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * @Author Xiaofeng
 * @Date 2013-6-3 обнГ6:36:20
 */

public class Utility {
	public static void writeToFile(String file, String content) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file);
			pw.write(content);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			pw.close();
		}

	}
}
