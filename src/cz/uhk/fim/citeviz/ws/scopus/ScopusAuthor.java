package cz.uhk.fim.citeviz.ws.scopus;

import java.util.Map;
import java.util.Set;

import cz.uhk.fim.citeviz.model.AuthorFullDetail;

public class ScopusAuthor extends AuthorFullDetail{
	
	private static final long serialVersionUID = 1L;
	
	private boolean completed = false;
	
	public ScopusAuthor(String id, String name, int citationIndex, Map<String, Set<String>> otherData) {
		super(id, name, citationIndex, otherData);
	}
	
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	public boolean isCompleted() {
		return completed;
	}
}