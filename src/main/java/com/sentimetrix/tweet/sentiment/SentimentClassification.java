package com.sentimetrix.tweet.sentiment;

public class SentimentClassification {

	private Sentiment sentiment;
	private double confidence;
	
	public SentimentClassification(Sentiment sentiment, double confidence) {
		this.sentiment = sentiment;
		this.confidence = confidence;
	}
	
	public Sentiment getSentiment() {
		return sentiment;
	}

	public void setSentiment(Sentiment sentiment) {
		this.sentiment = sentiment;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	@Override 
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = "\n";
		
		result.append(this.getClass().getName() + " Object {" + NEW_LINE);
		result.append(" sentiment: " + sentiment + NEW_LINE);
		result.append(" confidence: " + confidence + NEW_LINE);
		result.append("}");

		return result.toString();
	}
}
