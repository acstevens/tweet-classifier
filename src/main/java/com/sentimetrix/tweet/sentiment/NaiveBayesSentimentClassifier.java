package com.sentimetrix.tweet.sentiment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.sentimetrix.tweet.util.StopWords;

public class NaiveBayesSentimentClassifier {
	private static final String[] POSITIVE_SMILEYS = {":)", ";)", ":D", ":-)", ":o)", ":-D"};
	private static final String[] NEGATIVE_SMILEYS = {":(", ":-(", ":'(", ":'-(", "D:"};
	private static final Set<String> POSITIVE_SMILEY_SET = new HashSet<String>(Arrays.asList(POSITIVE_SMILEYS));
	private static final Set<String> NEGATIVE_SMILEY_SET = new HashSet<String>(Arrays.asList(NEGATIVE_SMILEYS));

	private Map<String, List<Integer>> wordSentimentOccurence;
	private Integer[] totalSentimentCount;
	private StopWords stopWords;
	
	public NaiveBayesSentimentClassifier(){
		wordSentimentOccurence = new HashMap<String, List<Integer>>();
		totalSentimentCount = new Integer[Sentiment.values().length];
		for(Sentiment sentiment : Sentiment.values()) {
			totalSentimentCount[sentiment.ordinal()] = 0;				
		}
		stopWords = new StopWords();
	}

	private List<String> getTokens(String text){
		StringTokenizer tokens = new StringTokenizer(text);
		ArrayList<String> words = new ArrayList<String>();
		
		while(tokens.hasMoreTokens()){
			String tmp = tokens.nextToken();
			if(!POSITIVE_SMILEY_SET.contains(tmp) && !NEGATIVE_SMILEY_SET.contains(tmp)) {
				tmp = tmp.toLowerCase();
	
				StringBuilder sb = new StringBuilder();
				
				// TODO porter stemmer
				for(char ch : tmp.toCharArray()){
					if(Character.isLetter(ch)){
						sb.append(ch);
					}
				}
				tmp = sb.toString();
				if(tmp.length() > 0 && !stopWords.getStopwordsSet().contains(tmp)){
					words.add(sb.toString());
				}
			}
		}
		
		return words;
	}

	// look for smilies and assign label
	public Sentiment extractLabel(String text){
		StringTokenizer tokens = new StringTokenizer(text);
		while(tokens.hasMoreTokens()){
			String token = tokens.nextToken();
			if(POSITIVE_SMILEY_SET.contains(token)){
				return Sentiment.POSITIVE;
			}
			else if(NEGATIVE_SMILEY_SET.contains(token)){
				return Sentiment.NEGATIVE;
			}
		}
		return Sentiment.NONE;
	}

	public void trainInstance(String text){
		List<String> tokens = getTokens(text);
		Sentiment tweetLabel = extractLabel(text);
		if(tweetLabel != Sentiment.NONE) {
			//add these words to the classifier
			updateClassifier(tokens, tweetLabel);
		} else {
			totalSentimentCount[Sentiment.NONE.ordinal()]++;
		}
	}
	
	private void updateClassifier(List<String> tokens, Sentiment sentiment){
		for(String token : tokens){
			if(wordSentimentOccurence.containsKey(token)){
				List<Integer> sentiments = wordSentimentOccurence.get(token);
				sentiments.set(sentiment.ordinal(), sentiments.get(sentiment.ordinal())+1);
			} else{
				//make a new array and put it
				List<Integer> sentiments = new ArrayList<Integer>(Sentiment.values().length);

				for(Sentiment s : Sentiment.values()) {
					sentiments.add(s.ordinal(), 0);
				}
				
				sentiments.set(sentiment.ordinal(), sentiments.get(sentiment.ordinal())+1);
				wordSentimentOccurence.put(token, sentiments);
			}
		}
		//update the overall document count
		totalSentimentCount[sentiment.ordinal()]++;
	}

	public Integer getDocumentCount(Sentiment sentiment) {
		return totalSentimentCount[sentiment.ordinal()]++;
	}
	
	public String getWordOccurs(Sentiment sentiment, int topN){
		StringBuilder sb = new StringBuilder();
		
		WordCount wpcset[] = new WordCount[wordSentimentOccurence.keySet().size()]; 
		
		String s;
		int t = 0;
		
		for(String word : wordSentimentOccurence.keySet()){
			wpcset[t++] = new WordCount(word, Math.sqrt(wordSentimentOccurence.get(word).get(sentiment.ordinal()) * 1.0 ));
		}
		
		Arrays.sort(wpcset);
		
		double frac;
		for(int i = 0; (i < topN || topN <= 0) && i < wpcset.length; i++){
			s = wpcset[i].getWord();
			frac = wpcset[i].getCount();
			
			sb.append(s);
			sb.append(":");
			sb.append(frac);
			sb.append("\n");
		}
		
		return sb.toString();
	}

	public SentimentClassification classify(String text){
		double[] labelProbs = new double[Sentiment.values().length];

		List<String> tokens = getTokens(text);		
		int maxLabelIdx = 0;
		
		for(Sentiment sentiment : Sentiment.values()) {
			if(Sentiment.NONE != sentiment) {
				labelProbs[sentiment.ordinal()] = calculateLabelProb(tokens, sentiment);
				maxLabelIdx = labelProbs[sentiment.ordinal()] > labelProbs[maxLabelIdx] ? sentiment.ordinal() : maxLabelIdx;
			}
		}
		
		//calc the confidence
		double conf = labelProbs[maxLabelIdx];
		labelProbs[maxLabelIdx] = 0;
		conf -= sumVector(labelProbs);
		
		return new SentimentClassification(Sentiment.get(maxLabelIdx), conf);
	}

	private double calculateLabelProb(List<String> tokens, Sentiment sentiment){
		
		//calculate the class probabilities
		double[] pClass = new double[Sentiment.values().length];
		int cSum = sumSentimentCounts(totalSentimentCount);
		int totalWordCount = 0;
		
		for(int i = 0; i < totalSentimentCount.length; i++) {
			if(Sentiment.NONE != Sentiment.get(i)) {
				pClass[i] = totalSentimentCount[i] * 1.0 / cSum; 
			}
		}
		
		for(String word : wordSentimentOccurence.keySet()){
			List<Integer> wordLabelCount = wordSentimentOccurence.get(word);
			totalWordCount += sumVector(wordLabelCount);
		}
		
		double p = 1.0;
		boolean foundOne = false;
		for(String token : tokens){
			if(wordSentimentOccurence.containsKey(token)){
				foundOne = true;
				List<Integer> occurences = wordSentimentOccurence.get(token);
				double pWordGivenClass = occurences.get(sentiment.ordinal()) / (double)(sumVector(occurences)); 
				double pWord = sumVector(occurences) * 1.0 / totalWordCount;
				p *= pWordGivenClass * pClass[sentiment.ordinal()] / pWord;
			}
		}
		return foundOne ? p : 0.0;
	}

	
	private double sumVector(double[] vector){
		double sum = 0.0;
		for(double d : vector) sum += d;
		return sum;
	}

	private int sumVector(List<Integer> vector){
		int sum = 0;
		for(Integer item : vector) {
			sum += item;
		}
		return sum;
	}

	private int sumSentimentCounts(Integer[] vector){
		int sum = 0;
		for(Sentiment sentiment : Sentiment.values()) {
			if(Sentiment.NONE != sentiment) {
				sum += vector[sentiment.ordinal()];
			}
		}		
		return sum;
	}



}
