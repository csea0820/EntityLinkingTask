package com.nec.scg.utility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
			}
		}
		return keywords;
	}
	
	public static void saveCache(Map<String,Set<String>> cache,String cache_file){
		StringBuilder sb = new StringBuilder();
		
		for (String key : cache.keySet()) {
			sb.append(key);
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
						if (contents.length < 2)break;
						for (int i = 1; i < contents.length; i++)
							value.add(contents[i]);
						cache.put(contents[1], value);
						
					}
					str = br.readLine();

				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return cache;
	}
}
