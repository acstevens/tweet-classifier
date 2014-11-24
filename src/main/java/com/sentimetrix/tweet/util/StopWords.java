package com.sentimetrix.tweet.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class StopWords {
	private static final String DEFAULT_STOPWORDS_FILE = "stopwords.txt";
	
	private Set<String> stopwordsSet = new HashSet<String>();
	
	public StopWords(){
		this(DEFAULT_STOPWORDS_FILE);
	}
	
	public StopWords(String filename) {
		load(filename);
	}
	
	public void load(String filename) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)));
			String line;
			while((line = in.readLine())!=null) {
				stopwordsSet.add(line.toLowerCase().trim());
			}
			in.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Set<String> getStopwordsSet() {
		return stopwordsSet;
	}

}
