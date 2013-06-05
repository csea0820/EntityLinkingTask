import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;


/**
 * @Author Xiaofeng
 * @Date 2013-6-4 ÏÂÎç3:17:29
 */

public class MergeResult {

	EvaluationMetric em = new EvaluationMetric();
	
	
	public void mergeResult(String resultDir)
	{
		File dir = new File(resultDir+"\\NAR\\");

		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			em.setM_all_relevant_pages(files.length);
			for (File file : files) {
				String name = file.getName();
				System.out.println("Merge file " + name);
				merge(file,new File(resultDir+"\\STEM\\"+name.replace("NAR_", "STEM_"))
				,new File(resultDir+"\\DP\\"+name.replace("NAR_", "DP_")));
			}
		} 
		System.out.println("Recall="+em.recall()+",Precision="+em.precision());
	}
	
	Set<String> readResult(File file)
	{
		Set<String> result = new TreeSet<String>();
		BufferedReader br = null;
		FileReader fr = null;
		String str;
		
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			str = br.readLine();
			while (str != null) {
				if (!str.contains("[") && !str.contains("]") && !str.contains("{") && !str.contains("}") && !str.contains("=") && !str.contains(":"))
					result.add(str.trim());
				str = br.readLine();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally
		{
			try {
				fr.close();
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		return result;
	}
	
	private void merge(File file, File file2, File file3) {
		Set<String> s1 = readResult(file);
		Set<String> s2 = readResult(file2);
		Set<String> s3 = readResult(file3);

		s1.addAll(s2);
		s1.addAll(s3);
		
		int match = 0;
		
		em.allReturnedPagesPlus(s1.size());
		String[] expectedResult =  file.getName().split("_");
		if (s1.contains(expectedResult[2]) || "NIL".equals(expectedResult[2]))
			{em.returnedRelevantPagesPlus();match = 1;}
		

		StringBuilder sb = new StringBuilder();
		for (String can: s1)
			sb.append(can).append("\n");
		
		Utility.writeToFile("D:\\TAC_RESULT\\TOTAL\\"+expectedResult[1]+"_"+expectedResult[2]+"_"+expectedResult[3].replace(".txt", "")+"_"+match+".txt", sb.toString());
		
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new MergeResult().mergeResult("D:\\TAC_RESULT");
	}

}
