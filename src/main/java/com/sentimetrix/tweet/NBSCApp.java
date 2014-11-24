package com.sentimetrix.tweet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;

import com.sentimetrix.tweet.input.JsonFileProcessor;
import com.sentimetrix.tweet.sentiment.NaiveBayesSentimentClassifier;
import com.sentimetrix.tweet.sentiment.Sentiment;
import com.sentimetrix.tweet.sentiment.SentimentClassification;

public class NBSCApp {
	private static String LANGUAGE = "en";
	private static Integer TOP_OCCURENCE_LIMIT = 20;
	
	public static void main(String[] args){
		
		String filename = args.length >= 1 ? args[0] : "tweets_en_positive_negative.json";
		List<String> data = new ArrayList<String>();
		//initialize the sentiment classifier
		NaiveBayesSentimentClassifier nbsc = new NaiveBayesSentimentClassifier();
        
		//read the file, and train each document
		
		JsonFileProcessor parser = new JsonFileProcessor(new File(filename));
		JSONObject json;
		
		String text;
		int count = 0;
		while (parser.hasNext()) {
            json = parser.next();
            if(json != null) {
            	if(LANGUAGE.equals(json.getString("lang"))) {
		            text = json.getString("text");
		            data.add(text);
		            count++;
            	}
            }
		}

		String[][] splitData = new String[2][];
		int[] splitCounts = new int[2];
		for(int i = 0; i < splitCounts.length; i++) {
			splitCounts[i] = 0;
		}
		splitData[0] = new String[count];
		splitData[1] = new String[count];
		Random rand=new Random();
		
		for(String datum : data) {
			int x = rand.nextInt(2);
			splitData[x][splitCounts[x]] = datum;
			splitCounts[x]++;
		}
		
        System.out.println(LANGUAGE + " tweets: " + count);
		System.out.println("Tweets used for training " + splitCounts[0]);
		System.out.println("Tweets used for validation " + splitCounts[1]);
		System.out.println("");
		
		for(int i = 0; i < splitCounts[0]; i++) {
			nbsc.trainInstance(splitData[0][i]);		
		}
		
		System.out.println("Positive Tweets used for training " + nbsc.getDocumentCount(Sentiment.POSITIVE));
		System.out.println("Negative Tweets used for training " + nbsc.getDocumentCount(Sentiment.NEGATIVE));
		System.out.println("Tweets with no sentiment " + nbsc.getDocumentCount(Sentiment.NONE));
		System.out.println("");

		//print out the positive and negative dictionary
		System.out.println("=== Positive Dictionary: Top " + TOP_OCCURENCE_LIMIT + " ===");
		System.out.println(nbsc.getWordOccurs(Sentiment.POSITIVE, TOP_OCCURENCE_LIMIT));
		System.out.println("=== Negative Dictionary: Top " + TOP_OCCURENCE_LIMIT + " ===");
		System.out.println(nbsc.getWordOccurs(Sentiment.NEGATIVE, TOP_OCCURENCE_LIMIT));
		
        //now go through and classify validation data as positive or negative
		int positivePositive = 0;
		int positiveNegative = 0;
		int negativeNegative = 0;
		int negativePostive = 0;
		System.out.println("=== First 50 classified tweets ===");
		for(int i = 0; i < splitCounts[1]; i++) {
			text = splitData[1][i];
			SentimentClassification c = nbsc.classify(text);
			Sentiment sentiment = nbsc.extractLabel(text);
			if(c.getSentiment() == sentiment) {
				if(c.getSentiment() == Sentiment.NEGATIVE) {
					negativeNegative++;
				} else {
					positivePositive++;
				}
			} else {
				if(sentiment == Sentiment.POSITIVE) {
					positiveNegative++;
				} else {
					negativePostive++;
				}
			}
			if(i < 50) {
				System.out.println(c.getSentiment() + ", " + c.getConfidence() + " -> " + text);
			}
			if(i%1000 == 0) {
				System.out.println("Processed " + i + " tweets");

			}
		}		
		System.out.println("=== Confusion Matrix ===");
		System.out.println("positive actual; positive classification: " + positivePositive);
		System.out.println("positive actual; negative classification: " + positiveNegative);
		System.out.println("negative actual; negative classification: " + negativeNegative);
		System.out.println("negative actual; positive classification: " + negativePostive);
			
	}

}
