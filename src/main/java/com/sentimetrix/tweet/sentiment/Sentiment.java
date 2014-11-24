package com.sentimetrix.tweet.sentiment;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum Sentiment {
	NONE(0), POSITIVE(1), NEGATIVE(2);
	
    private static final Map<Integer,Sentiment> lookup = new HashMap<Integer,Sentiment>();

	static {
	    for(Sentiment s : EnumSet.allOf(Sentiment.class))
	         lookup.put(s.getValue(), s);
	}

	private int value;
	
	private Sentiment(int value) {
		this.value = value;
	}
	
	public int getValue() { return value; }

	public static Sentiment get(int value) {
		return lookup.get(value);
	}
};
