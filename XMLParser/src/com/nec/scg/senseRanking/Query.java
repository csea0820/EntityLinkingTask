/**
 * @Author Xiaofeng
 * @Date 2013-7-24 ионГ11:05:25
 */
package com.nec.scg.senseRanking;


public class Query implements Comparable<Query>{
	int id;
	String query;
	
	
	public Query(String query) {
		this.query = query;
	}
	
	public Query(String query,int id) {
		this(query);
		this.id = id;
	}

	@Override
	public int compareTo(Query o) {
		return id-o.id;
	}
	
	@Override
	public String toString() {
		return id+"_"+query;
	}
}