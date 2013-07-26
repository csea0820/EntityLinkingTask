/**
 * @Author Xiaofeng
 * @Date 2013-7-24 ÉÏÎç11:05:25
 */
package com.nec.scg.senseRanking;

import java.util.ArrayList;
import java.util.List;


public class Query implements Comparable<Query>{
	
	int id;
	String query;
	String docID;
	
	
	public Query(int id) {
		this.id = id;
	}
	
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
	
	public void setDocID(String docID) {
		this.docID = docID;
	}
	
	public String getDocID() {
		return docID;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public static List<Query> getQueryInfo(String file){
		List<Query> ret = new ArrayList<Query>();
		
		return ret;
	}
}