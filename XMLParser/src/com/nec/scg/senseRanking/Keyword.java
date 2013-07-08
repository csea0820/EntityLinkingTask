/**
 * @Author Xiaofeng
 * @Date 2013-6-24 ÏÂÎç2:04:23
 */
package com.nec.scg.senseRanking;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


public class Keyword implements Comparable<Keyword>{

	int index;
	String keyword;
	String targetPageName;
	
	public Keyword(int  index)
	{
		this.index = index;
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
		return o.index - index;
	}
	
	@Override
	public String toString() {
		return index+"";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		Map<Keyword,String> map = new TreeMap<Keyword,String>();
//		map.put(new Keyword("A","B"), "C");
//		map.put(new Keyword("B","J"), "D");
//			System.out.println(map.get("A"));
		
		Set<Keyword> set = new TreeSet<Keyword>();
		set.add(new Keyword(2));
		set.add(new Keyword(1));
		set.add(new Keyword(3));
		System.out.println(set);
		
	}
}
