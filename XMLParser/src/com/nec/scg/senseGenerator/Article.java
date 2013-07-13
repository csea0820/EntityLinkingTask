package com.nec.scg.senseGenerator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Article implements Comparable<Article> {
	public String articleName;
//	int id;
	public int countAllLink;  //用来统计连接数的
	
	private double link_prob = 0.0;
	Set<String> referencedArticle = null;

	Set<String> dps = null;
	Set<String> eab = null;
	Set<String> setm = null;
	Set<String> redirectNames = null;

	Map<String,Integer> sourceStatistic = null;
	String normalizedName = null;

	Article() {
//		referencedArticle = new TreeSet<String>();
//		dps = new TreeSet<String>();
		setm = new TreeSet<String>();
		sourceStatistic = new TreeMap<String,Integer>();
//		eab = new TreeSet<String>();
//		redirectNames = new TreeSet<String>();
	}

	Article(String articleName) {
		this();
		this.articleName = articleName;
		//getArticleName();
	}

//	public void setId(int id) {
//		this.id = id;
//	}
//
//	public int getId() {
//		return id;
//	}

	public String getArticleName() {
		return articleName;
	}

	public void setArticleName(String articleName) {
		this.articleName = articleName;
	}

	public Set<String> getReferencedArticle() {
		return referencedArticle;
	}

	public void addReferencedArticle(String article) {
		//referencedArticle.add(article);
	}

	public void addSource(String source){
		Integer cnt = sourceStatistic.get(source);
		if (cnt == null)sourceStatistic.put(source, 1);
		else sourceStatistic.put(source, cnt+1);
	}
	
	public void addSETM(String source) {
		setm.add(source);

	}

	public void setDps(Set<String> dps) {
		this.dps = dps;
	}

	public void addDP(String dp) {
		//dps.add(dp);
		setm.add(dp);
	}

	public void setLink_prob(double link_prob) {
		this.link_prob = link_prob;
	}
	
	public void addEAB(String shortName) {
		//eab.add(shortName);
		setm.add(shortName.toLowerCase());
	}

	public Set<String> getSetm() {
		return setm;
	}
	
	public Map<String, Integer> getSourceStatistic() {
		return sourceStatistic;
	}
	
	public String getNormalizedName() {
		if (normalizedName == null) {
			if (articleName.contains("(") && articleName.contains(")")) {
				normalizedName = articleName
						.substring(0, articleName.indexOf("(")).toLowerCase()
						.trim();
			} else {
				normalizedName = articleName.toLowerCase().trim();
			}
		}

		return normalizedName;
	}

	public void setNormalizedName(String normalizedName) {
		this.normalizedName = normalizedName;
	}

	public Set<String> getRedirectNames() {
		return redirectNames;
	}

	public void addRedirectName(String redirectName) {
		//redirectNames.add(redirectName);
		setm.add(redirectName);
	}

	@Override
	public int compareTo(Article o) {
		return articleName.compareTo(o.articleName);
	}
	
	public boolean match(String query)
	{
		
//		if (articleName.equals(query))return true;
//		if (articleName.contains(query))return true;
		if (setm.contains(query))return true;
//		if (eab.contains(query))return true;
//		if (dps.contains(query))return true;
		
		
		return false;
	}
}
