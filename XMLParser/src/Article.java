import java.util.Set;
import java.util.TreeSet;

public class Article implements Comparable<Article> {
	String articleName;
//	int id;
	Set<String> referencedArticle = null;

	Set<String> dps = null;
	Set<String> eab = null;
	Set<String> setm = null;
	Set<String> redirectNames = null;

	
	String normalizedName = null;

	Article() {
		referencedArticle = new TreeSet<String>();
		dps = new TreeSet<String>();
		setm = new TreeSet<String>();
		eab = new TreeSet<String>();
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

	public void addEAB(String shortName) {
		//eab.add(shortName);
		setm.add(shortName);
	}

	public Set<String> getSetm() {
		return setm;
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
		
		if (articleName.equals(query))return true;
		if (articleName.contains(query))return true;
		if (setm.contains(query))return true;
		if (eab.contains(query))return true;
		if (dps.contains(query))return true;
		
		
		return false;
	}
}
