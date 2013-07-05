/**
 * @Author Xiaofeng
 * @Date 2013-6-24 ÏÂÎç2:04:23
 */
package com.nec.scg.senseRanking;

import java.util.Map;
import java.util.TreeMap;


public class Keyword implements Comparable<Keyword>{

	String keyword;
	String targetPageName;
	
	public Keyword(String keyword)
	{
		this.keyword = keyword;
	}
	
	public Keyword(String keyword,String targetPageName)
	{
		this.keyword = keyword;
		this.targetPageName = targetPageName;
	}
	
	
	
	public String getKeyword() {
		return keyword;
	}


	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}



	public String getTargetPageName() {
		return targetPageName;
	}



	public void setTargetPageName(String targetPageName) {
		this.targetPageName = targetPageName;
	}


	@Override
	public int compareTo(Keyword o) {
		return keyword.compareTo(o.keyword);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<Keyword,String> map = new TreeMap<Keyword,String>();
		map.put(new Keyword("A","B"), "C");
		map.put(new Keyword("B","J"), "D");
			System.out.println(map.get("A"));
		
	}
}
