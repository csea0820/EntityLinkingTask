package com.nec.scg.senseGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import com.nec.scg.senseRanking.ArticleAttributes;
import com.nec.scg.utility.Utility;


/**
 * @Author Xiaofeng
 * @Date 2013-6-4 下午3:17:29
 */

public class MergeResult {

	EvaluationMetric em = new EvaluationMetric();
	
	Set<String> kbNames = null;
	
	public void mergeResult(String resultDir)
	{
		File dir = new File(resultDir+"\\NAR\\");

		KnowledgeBase kb = new KnowledgeBase();
		kbNames = kb.getKbArticleNames();
		
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			em.setM_all_relevant_pages(files.length); //all_relevant_pages=query总条数3904
			for (File file : files) {
				String name = file.getName();
//				System.out.println("Merge file " + name);
				//合并时是将相同的query的stem、nar、dp的结果进行合并，因此只需文件名部分NAR用stem或dp来地替代就是stem、dp中相同query的结果
				merge(file,new File(resultDir+"\\STEM\\"+name.replace("NAR_", "STEM_"))
				,new File(resultDir+"\\DP\\"+name.replace("NAR_", "DP_")));
			}
		} 
		System.out.println("all_relevant_pages="+em.m_all_relevant_pages+","+"all_returned_pages="+em.m_all_returned_pages+","+"m_returned_relevant_pages="+em.m_returned_relevant_pages
		+"\nRecall="+em.recall()+",Precision="+em.precision());
	}
	
	
	Set<ArticleAttributes> readNARResult(File file)
	{
		Set<ArticleAttributes> result = new TreeSet<ArticleAttributes>(new Comparator<ArticleAttributes>() {

			@Override
			public int compare(ArticleAttributes o1, ArticleAttributes o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		BufferedReader br = null;
		FileReader fr = null;
		String str;
		//合并前将结果再做一次过滤，将有{，{，=，：之类的行都去掉
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			str = br.readLine();
			while (str != null) {
				if (!str.contains("[") && !str.contains("]") && !str.contains("{") && !str.contains("}") && !str.contains("=") && !str.contains(":"))
					{
						String[] contents = str.trim().split("\t");
						result.add(new ArticleAttributes(contents[0], Boolean.parseBoolean(contents[2]), Boolean.parseBoolean(contents[1])));
					}
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
	
	Set<String> readResult(File file)
	{
		Set<String> result = new TreeSet<String>();
		BufferedReader br = null;
		FileReader fr = null;
		String str;
		//合并前将结果再做一次过滤，将有{，{，=，：之类的行都去掉
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
		Set<ArticleAttributes> s1 = readNARResult(file);
		Set<String> s2 = readResult(file2);
		Set<String> s3 = readResult(file3);

		for (String s : s2)
		{
			ArticleAttributes art = new ArticleAttributes(s);
			if (!s1.contains(art)){
				s1.add(art);
			}
		}
		
		for (String s : s3)
		{
			ArticleAttributes art = new ArticleAttributes(s);
			if (!s1.contains(art)){
				s1.add(art);
			}
		}
		
		int match = 0;
		
		
		
		
		String[] expectedResult =  file.getName().split("_");
		if (!"NIL".equals(expectedResult[2]) && !kbNames.contains(expectedResult[2])){
			System.out.println(expectedResult[1]+","+expectedResult[2]);
		}
		//如果结果中包含query name的或者结果为nil的都算是正确或匹配，则returnedRelevantPages+1
		if ((s1.contains(new ArticleAttributes(expectedResult[2])) && kbNames.contains(expectedResult[2]))  || "NIL".equals(expectedResult[2]))
		{
			em.returnedRelevantPagesPlus();match = 1;
			
		}
			
		StringBuilder sb = new StringBuilder();
		for (ArticleAttributes can: s1)
			if (kbNames.contains(can.getName()))
			{
				em.allReturnedPagesPlus(1);
				sb.append(can.getName()).append("\t").append(can.isSubstr_test()).append("\t").append(can.isEditDistance_test()).append("\n");
			}
		Utility.writeToFile("D:\\TAC_RESULT\\TOTAL\\"+match+"_"+expectedResult[1]+"_"+expectedResult[2]+"_"+expectedResult[3].replace(".txt", "")+".txt", sb.toString());
		
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new MergeResult().mergeResult("D:\\TAC_RESULT");
	}

}
