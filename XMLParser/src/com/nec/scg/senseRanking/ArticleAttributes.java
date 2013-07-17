/**
 * @Author Xiaofeng
 * @Date 2013-7-13 ÏÂÎç3:55:10
 */
package com.nec.scg.senseRanking;


public class ArticleAttributes implements Comparable<ArticleAttributes> {
	String name;
	
	
	double link_prob;
	boolean editDistance_test;
	boolean substr_test;
	
	double ctx_sim; 
	double ctx_wt;
	int ctx_ct;
	double link_combo;
	
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
		link_combo = 0.5*ctx_sim+0.7*link_prob;
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
		return name+"\t"+link_prob+"\t"+ctx_sim+"\t"+link_combo+"\n";
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
}
