import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Xiaofeng
 * @Date 2013-5-30 ÉÏÎç11:04:49
 */

public class ExpectedLinkResult {

	
	List<String> queries = null;
	List<String> expectedResult = null;
	
	ExpectedLinkResult() {
		queries = new ArrayList<String>();
		expectedResult = new ArrayList<String>();
	}
	
	
	public void readLinkResult(String filePath)
	{
		File file = new File(filePath);
		
		BufferedReader br = null;
		FileReader fr = null;
		String str;
		
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			str = br.readLine();
			
			while (str != null)
			{
				
				String[] contents = str.split("\t");
				if (contents.length < 2)
				{
					System.err.println("FORMAT ERROR IN " + filePath);
					continue;
				}
				
				queries.add(contents[0].trim());
				expectedResult.add(contents[1].trim());
				
				str = br.readLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> getExpectedResult() {
		return expectedResult;
	}
	
	public List<String> getQueries() {
		return queries;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExpectedLinkResult elr = new ExpectedLinkResult();
		elr.readLinkResult("linkresult");

		int i = 0;
		for (i = 0; i < elr.getQueries().size(); i++)
		{
			System.out.println(elr.getQueries().get(i)+","+elr.getExpectedResult().get(i));
		}
	}

}
