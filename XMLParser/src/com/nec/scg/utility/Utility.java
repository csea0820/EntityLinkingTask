package com.nec.scg.utility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.nec.scg.senseRanking.ArticleAttributes;
import com.nec.scg.senseRanking.Query;

/**
 * @Author Xiaofeng
 * @Date 2013-6-3 ÏÂÎç6:36:20
 */

public class Utility {
	public static void writeToFile(String file, String content) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file);
			pw.write(content);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			pw.close();
		}

	}
	
	

	public static int getEditDistance(String s, String t) {
		int d[][]; // matrix
		int n; // length of s
		int m; // length of t
		int i; // iterates through s
		int j; // iterates through t
		char s_i; // ith character of s
		char t_j; // jth character of t
		int cost; // cost

		n = s.length();
		m = t.length();
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		d = new int[n + 1][m + 1];

		for (i = 0; i <= n; i++) {
			d[i][0] = i;
		}

		for (j = 0; j <= m; j++) {
			d[0][j] = j;
		}

		for (i = 1; i <= n; i++) {
			s_i = s.charAt(i - 1);
			for (j = 1; j <= m; j++) {
				t_j = t.charAt(j - 1);
				if (s_i == t_j) {
					cost = 0;
				} else {
					cost = 1;
				}
				d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1),
						d[i - 1][j - 1] + cost);
			}
		}
		return d[n][m];

	}
	
	public static Set<String> getKeywords(String f)
	{
		Set<String> keywords = new TreeSet<String>();
		
		
		File file = new File(f);
		if (file.exists()) {
			BufferedReader br = null;
			FileReader fr = null;
			String str;

			try {
				fr = new FileReader(file);
				br = new BufferedReader(fr);

				str = br.readLine();
				
				while (str != null)
				{
					keywords.add(str.replaceAll("¡±", "").replaceAll("¡°", "").toLowerCase());
					str = br.readLine();
				}
			}catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				try {
					fr.close();
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return keywords;
	}
	
	public static  void appendContentToFile(String filePath,String content) {
		
		
		
		try {
			File file = new File(filePath);
			if (file.exists() == false)
				file.createNewFile();
			
			OutputStreamWriter out = new OutputStreamWriter(
					new FileOutputStream(filePath,true), "UTF-8");
			out.write(content);
			out.flush();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveCache(Map<String,Set<String>> cache,String cache_file){
		StringBuilder sb = new StringBuilder();
		
		for (String key : cache.keySet()) {
			sb.append(key).append("\t");
			
			for (String value : cache.get(key))
				sb.append(value).append("\t");
			sb.append("\n");
		}

		Utility.writeToFile(cache_file, sb.toString());
	}
	
	public static Map<String,Set<String>> readCache(String cache_file){
		File file = new File(cache_file);
		Map<String,Set<String>> cache = new TreeMap<String,Set<String>>();
		
		if (file.exists()) {
			BufferedReader br = null;
			FileReader fr = null;
			String str;

			try {
				fr = new FileReader(file);
				br = new BufferedReader(fr);

				str = br.readLine();

				while (str != null) {
					if (!str.equals("")) {
						Set<String> value = new TreeSet<String>();
						String[] contents = str.split("\t");
						for (int i = 1; i < contents.length; i++)
							value.add(contents[i]);
						cache.put(contents[0], value);
						
					}
					str = br.readLine();

				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				
				try {
					fr.close();
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return cache;
	}
	
	public static Map<Query,Set<ArticleAttributes>> readCandidates(String diretory) {

		Map<Query,Set<ArticleAttributes>> candidates = new TreeMap<Query,Set<ArticleAttributes>>();
		File dir = new File(diretory);

		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {

				String[] queryInfo = file.getName().split("_");
				String query = queryInfo[1];

				int id = Integer.parseInt(queryInfo[3].substring(0, queryInfo[3].length()-4));
				Set<ArticleAttributes> candidate = null;

//				if (candidates.containsKey(query))
//					continue;

				BufferedReader br = null;
				FileReader fr = null;
				String str;

				try {
					fr = new FileReader(file);
					br = new BufferedReader(fr);

					str = br.readLine();
					candidate = new TreeSet<ArticleAttributes>();
					while (str != null) {
						String[] contents = str.split("\t");
						if (contents.length == 3)
							candidate.add(new ArticleAttributes(contents[0], Boolean.parseBoolean(contents[2]), Boolean.parseBoolean(contents[1])));
						str = br.readLine();
					}
					candidates.put(new Query(query,id), candidate);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					try {
						fr.close();
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		}
		
		return candidates;

	}
}
