/**
 * @Author Xiaofeng
 * @Date 2013-7-13 обнГ3:55:10
 */
package com.nec.scg.senseRanking;


public class ArticleAttributes implements Comparable<ArticleAttributes> {
	String name;
	double link_prob;
	double ctx_sim; 
	double link_combo;
	
	public ArticleAttributes(String name)
	{	
		this.name = name;
	}
	
	public ArticleAttributes(String name,double ctx_sim){
		this.name = name;
		this.ctx_sim = ctx_sim;
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
}
