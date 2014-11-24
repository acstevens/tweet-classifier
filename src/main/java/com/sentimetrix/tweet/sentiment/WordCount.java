package com.sentimetrix.tweet.sentiment;

public class WordCount implements Comparable<WordCount>{

	private String word;
	private Double count;
	
	public WordCount(String word, Double count){
		this.word = word;
		this.count = count;
	}
	
	public int compareTo(WordCount o) {
		return o.count.compareTo(count);
	}

    public boolean equals(Object o) {
        if (!(o instanceof WordCount))
            return false;
        WordCount w = (WordCount) o;
        return w.word.equals(word) && w.count.equals(count);
    }

    public int hashCode() {
        return word.hashCode() + count.hashCode();
    }

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public double getCount() {
		return count;
	}

	public void setCount(Double count) {
		this.count = count;
	}
 
}
