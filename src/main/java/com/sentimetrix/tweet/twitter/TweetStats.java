package com.sentimetrix.tweet.twitter;


import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.sentimetrix.tweet.input.JsonFileProcessor;
import com.sentimetrix.tweet.sentiment.Sentiment;

public class TweetStats
{
	private static final String[] POSITIVE_SMILEYS = {":)", ";)", ":D", ":-)", ":o)", ":-D", "=)", ": )"};
	private static final String[] NEGATIVE_SMILEYS = {":(", ":(", ":-(", ":'(", ":'-(", "D:", ": ("};
	private static final String[] TONGUE_SMILEYS = {":P", ":p"};
	private static final Set<String> POSITIVE_SMILEY_SET = new HashSet<String>(Arrays.asList(POSITIVE_SMILEYS));
	private static final Set<String> NEGATIVE_SMILEY_SET = new HashSet<String>(Arrays.asList(NEGATIVE_SMILEYS));
	private static final Set<String> TONGUE_SMILEY_SET = new HashSet<String>(Arrays.asList(TONGUE_SMILEYS));
	private static Map<String, Integer> emoticonCounts = new HashMap<String, Integer>();

	
	private static Sentiment extractLabel(String text){
		boolean hasTongue = false;
		
		String emoticonFound = null;
		if((emoticonFound = searchSet(POSITIVE_SMILEY_SET, text)) != null){
			incrementEmoticonCounts(emoticonCounts, emoticonFound);
			return Sentiment.POSITIVE;
		} else if((emoticonFound = searchSet(NEGATIVE_SMILEY_SET, text)) != null){
			incrementEmoticonCounts(emoticonCounts, emoticonFound);
			return Sentiment.NEGATIVE;
		} else if((emoticonFound = searchSet(TONGUE_SMILEY_SET, text)) != null){
			incrementEmoticonCounts(emoticonCounts, emoticonFound);
			hasTongue = true;
		}
		
		if(!hasTongue) {
			System.out.println(text);
		}
		return Sentiment.NONE;
	}

	private static String searchSet(Set<String> set, String value) {
		if(value != null) {
			for(String item : set) {
				if(value.contains(item)) {
					return item;
				}
			}
		}
		
		return null;
	}
	
	private static void incrementEmoticonCounts(Map<String, Integer> counts, String emoticon) {
		if(counts.get(emoticon) == null) {
			counts.put(emoticon, 0);
		}
		counts.put(emoticon, counts.get(emoticon) + 1);
	}
		
    public static void main(String[] args)
    {
    	String filename = args.length >= 1 ? args[0] : "searchresults_lang:fr :) OR :(.json";
    	Integer[] labelCounts = new Integer[Sentiment.values().length];
    	Integer retweetedCount = 0;
    	
		for(Sentiment sentiment : Sentiment.values()) {
			labelCounts[sentiment.ordinal()] = 0;				
		}

    	JsonFileProcessor parser = new JsonFileProcessor(new File(filename));
		JSONObject json;
		
		int count = 0;
		Map<String, Integer> languageCounts = new HashMap<String, Integer>();
		
		while (parser.hasNext()) {
            json = parser.next();
            count++;
            String language = json.getString("lang");
            String text = json.getString("text");
            if(json.has("retweeted_status")) {
            	retweetedCount++;
            } else {         
	            Sentiment tweetLabel = extractLabel(text);
	            labelCounts[tweetLabel.getValue()]++;
            }
            
            if(languageCounts.get(language) == null) {
            	languageCounts.put(language, 0);
            }
            
            languageCounts.put(language,languageCounts.get(language)+1);
		}

		System.out.println("=== Big Picture ===");

		System.out.println("Tweet count: " + count);
		
		for(String language: languageCounts.keySet()) {
			System.out.println(language + " count: " + languageCounts.get(language));
		}
		System.out.println("Retweet count (filtered): " + retweetedCount);
		
		int tongueCount = 0;
		for(String e: TONGUE_SMILEY_SET) {
			tongueCount += emoticonCounts.get(e);
		}
		System.out.println("Tongue count (appear in the NONE sentiment count): " + tongueCount);

		System.out.println("=== Sentiment Counts ===");
		for(Sentiment sentiment : Sentiment.values()) {
			System.out.println(sentiment.name() + " count: " + labelCounts[sentiment.getValue()]);
		}
		
		System.out.println("=== Positive/Negative Emoticon Counts ===");
		for(String emoticon: emoticonCounts.keySet()) {
			System.out.println(emoticon + " count: " + emoticonCounts.get(emoticon));
		}
    }
}

