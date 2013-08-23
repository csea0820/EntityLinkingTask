/**
 * @Author Xiaofeng
 * @Date 2013-7-20 ÏÂÎç4:14:57
 */
package com.nec.scg.senseRanking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.nec.scg.senseGenerator.ExpectedLinkResult;
import com.nec.scg.utility.Utility;

public class TrainingSetGen {

	private String outputFile = "D:\\TAC_RESULT\\training.arff";

	private int topN = 3; // number of senses for every query ranked to position
							// N is used for training

	private StringBuilder builder = new StringBuilder();
	
	private int trueInstanceCnt = 0;
	private int falseInstanceCnt = 0;
	
	public void trainingSetGenerate(String instancesDir,
			String expectedResultFile) {

		ExpectedLinkResult elr = new ExpectedLinkResult();
		elr.readLinkResult(expectedResultFile);

		builder.append(ArticleAttributes.getArffHeader());
		builder.append("@data\n");
		int instancesCnt = 0;
		File dir = new File(instancesDir);
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				String [] queryInfo = file.getName().substring(0,
						file.getName().length() - 4).split("_");
				String query = queryInfo[1];
				String expectedResult = elr.getExpectedResult(Integer.parseInt(queryInfo[0]));
				
				if (expectedResult.equals("NIL"))continue;
				
				List<ArticleAttributes> topNSenses = getTopNSenses(file);
				for (ArticleAttributes art : topNSenses)
				{
					if (expectedResult.equals(art.getName()))
					{
						art.setCorrectSense(true);
						trueInstanceCnt++;
					}
					
					builder.append(art.toArffFormat());
					instancesCnt++;
				}
			}
		}
		falseInstanceCnt = instancesCnt - trueInstanceCnt;
		System.out.println("Training Set Size : " + instancesCnt+",TrueInstancesCnt:"+trueInstanceCnt);
		Utility.writeToFile(outputFile, builder.toString());
	}
	

	

	private List<ArticleAttributes> getTopNSenses(File file) {
		List<ArticleAttributes> ret = new ArrayList<ArticleAttributes>();

		BufferedReader br = null;
		FileReader fr = null;
		String str;

		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			str = br.readLine();
			int count = 0;
			while (str != null && count++ != topN) {

				ret.add(ArticleAttributes.getArticleFromString(str));

				str = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fr.close();
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return ret;
	}
	
	public String getOutputFile() {
		return outputFile;
	}
	
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TrainingSetGen set = new TrainingSetGen();
		set.trainingSetGenerate("D:\\TAC_RESULT\\linkComboRanking", "linkresult");
	}

}
