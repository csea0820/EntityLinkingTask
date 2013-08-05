/**
 * @Author Xiaofeng
 * @Date 2013-7-13 ÏÂÎç3:55:10
 */
package com.nec.scg.senseRanking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;


public class ArticleAttributes implements Comparable<ArticleAttributes> {
	String name;
	
	
	double link_prob;
	boolean editDistance_test;
	boolean substr_test;
	
	double ctx_sim; 
	double ctx_wt;
	int ctx_ct;
	double link_combo;
	
	boolean correctSense; // used only for training
	
	public ArticleAttributes(String name)
	{	
		this.name = name;
	}
	
	public ArticleAttributes(String name,double ctx_sim){
		this.name = name;
		this.ctx_sim = ctx_sim;
	}
	
	
	
	public ArticleAttributes(String name,boolean editDistance_test, boolean substr_test) {
		this(name);
		this.editDistance_test = editDistance_test;
		this.substr_test = substr_test;
	}

	public void setEditDistance_test(boolean editDistance_test) {
		this.editDistance_test = editDistance_test;
	}
	
	public boolean isEditDistance_test() {
		return editDistance_test;
	}
	
	
	public String getName() {
		return name;
	}
	
	public void setCtx_sim(double ctx_sim) {
		this.ctx_sim = ctx_sim;
	}
	
	public void setLink_prob(double link_prob) {
		this.link_prob = link_prob;
	}
	
	public void calLink_combo()
	{
		link_combo = 0.5*ctx_sim+2*link_prob;
	}
	
	public double getLink_combo(){
		return link_combo;
	}

	@Override
	public int compareTo(ArticleAttributes o) {
		if (o.link_combo - link_combo > 0)
			return 1;
		return -1;
	}
	
	@Override
	public String toString() {
		return name+"\t"+link_prob+"\t"+editDistance_test+"\t"+substr_test+"\t"+ctx_sim+"\t"
				+ctx_wt+"\t"+ctx_ct+"\t"+link_combo+"\n";
	}
	
	public static ArticleAttributes getArticleFromString(String content){
		
		String[] contents = content.split("\t");

		ArticleAttributes articleAttr = new ArticleAttributes(
				contents[0]);
		articleAttr.setLink_prob(Double.parseDouble(contents[1]));
		articleAttr.setEditDistance_test(Boolean.parseBoolean(contents[2]));
		articleAttr.setSubstr_test(Boolean.parseBoolean(contents[3]));
		articleAttr.setCtx_sim(Double.parseDouble(contents[4]));
		articleAttr.setCtx_wt(Double.parseDouble(contents[5]));
		articleAttr.setCtx_ct(Integer.parseInt(contents[6]));
		articleAttr.setLink_prob(Double.parseDouble(contents[7]));
		articleAttr.calLink_combo();
		
		
		return articleAttr;
	}
	
	public static String getArffHeader(){
		StringBuilder sb = new StringBuilder();
		
		sb.append("@relation SenseAttribute\n");
		sb.append("@attribute LINK_PROB real\n");
		sb.append("@attribute EDITDIST_TEST real\n");
		sb.append("@attribute SUBSTR_TEST real\n");
		sb.append("@attribute CTX_SIM real\n");
		sb.append("@attribute CTX_WT real\n");
		sb.append("@attribute CTX_CT real\n");
		sb.append("@attribute LINK_COMBO real\n");
		sb.append("@attribute CORRECT {true,false}\n");
	
		return sb.toString();
	}
	
	public Instance toWekaInstance(Instances instances){
		double[] instanceValue = new double[instances.numAttributes()];
		instanceValue[0] = link_prob;
		instanceValue[1] = editDistance_test==true?1:0;
		instanceValue[2] = substr_test==true?1:0;
		instanceValue[3] = ctx_sim;
		instanceValue[4] = ctx_wt;
		instanceValue[5] = ctx_ct;
		instanceValue[6] = link_combo;
//		instanceValue[7] = 0;

		return new DenseInstance(1.0,instanceValue);
	}
	
	public String toArffFormat(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(link_prob).append(",").append(isEditDistance_test()?1:0).append(",").append(substr_test?1:0).
		append(",").append(ctx_sim).append(",").append(ctx_wt).append(",").append(ctx_ct).append(",").append(link_combo)
		.append(",").append(correctSense).append("\n");
		
		return sb.toString();
	}

	public boolean isSubstr_test() {
		return substr_test;
	}

	public void setSubstr_test(boolean substr_test) {
		this.substr_test = substr_test;
	}

	public double getCtx_wt() {
		return ctx_wt;
	}

	public void setCtx_wt(double ctx_wt) {
		this.ctx_wt = ctx_wt;
	}

	public int getCtx_ct() {
		return ctx_ct;
	}

	public void setCtx_ct(int ctx_ct) {
		this.ctx_ct = ctx_ct;
	}

	public double getCtx_sim() {
		return ctx_sim;
	}
	
	public void setCorrectSense(boolean correctSense) {
		this.correctSense = correctSense;
	}
	
	public boolean isCorrectSense() {
		return correctSense;
	}
	
	public void setLink_combo(double link_combo) {
		this.link_combo = link_combo;
	}
	
	public static List<ArticleAttributes> readSenses(File candiatesFile) {
		List<ArticleAttributes> ret = new ArrayList<ArticleAttributes>();

		BufferedReader br = null;
		FileReader fr = null;
		String str;

		try {
			fr = new FileReader(candiatesFile);
			br = new BufferedReader(fr);

			str = br.readLine();
			int count = 0;
			while (str != null && count != 3) {

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
	
	public double getLink_prob() {
		return link_prob;
	}
	
}
