package com.linguistic.rake;

import java.util.Map;

public class KeywordsExtractor {
	private Rake rake = new Rake(RakeLanguages.ENGLISH);
	
	public Map<String, Double> analyzeText(String text){
	     return rake.getKeywordsFromText(text);
	}
}
