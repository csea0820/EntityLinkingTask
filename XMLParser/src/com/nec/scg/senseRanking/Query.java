/**
 * @Author Xiaofeng
 * @Date 2013-7-24 ÉÏÎç11:05:25
 */
package com.nec.scg.senseRanking;

import java.io.File;

import com.nec.scg.utility.Constant;


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
	
	
	public File getDocument(){
		String path = Constant.queryDocDirectory;
		String[] str = docID.split("[_||\\.]");
		path += str[0].toLowerCase()+"_"+str[1].toLowerCase()+"\\";
		path += str[2]+"\\";
		return new File(path+docID+".sgm");
	}
	
	public int getId() {
		return id;
	}
	
	public String getQuery() {
		return query;
	}
	
	@Override
	public boolean equals(Object obj) {
		Query o = (Query) obj;
		return this.id == o.id;
	}
	
}