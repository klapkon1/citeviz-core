package cz.uhk.fim.citeviz.ws.custom;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cz.uhk.fim.citeviz.model.Author;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.Paper;

public class DocToObjectConverter {

	public static final Author convertAuthor(Element e){
		Author a = new Author(getTagValue("id", e), getTagValue("name", e));
		
		//naètení èlánkù autora
		NodeList papersList = e.getElementsByTagName("paper-id");
		for (int j = 0; j < papersList.getLength(); j++) {
			a.getPapersId().add(new IdRecord(papersList.item(j).getTextContent()));
		}
		
		//a.setCitovanost(Integer.parseInt(getTagValue("rank", clElement)));
		
		//naètení spolupracovníkù autora
		NodeList collList = e.getElementsByTagName("coll-id");
		for (int j = 0; j < collList.getLength(); j++) {
			a.getCollaborators().add(new IdRecord(collList.item(j).getTextContent()));
		}
		
		
		//naètení autorù, kteøí citují daného autora
		NodeList childList = e.getElementsByTagName("cit-id");
		for (int j = 0; j < childList.getLength(); j++) {
			a.getChilds().add(new IdRecord(childList.item(j).getTextContent()));
		}
		
		//naètení autorù, které tento autor cituje
		NodeList refList = e.getElementsByTagName("ref-id");
		for (int j = 0; j < refList.getLength(); j++) {
			a.getParents().add(new IdRecord(refList.item(j).getTextContent()));
		}
		
		return a;
		
	}
	
	public static final Paper convertPaper(Element e){
		
		Set<Author> authors = new HashSet<Author>();
		NodeList auList = e.getElementsByTagName("author");
			
		for (int j = 0; j < auList.getLength(); j++) {
			Element auElement = (Element) auList.item(j);
			Author a = new Author(getTagValue("id", auElement), getTagValue("name", auElement));
			authors.add(a);
		}
		

		Paper p = new Paper(getTagValue("id", e),
					          getTagValue("title", e),
					          authors,
					          Integer.parseInt(getTagValue("year", e)),
					          Integer.parseInt(getTagValue("rank", e))
					          );
		
		NodeList refList = e.getElementsByTagName("ref-id");
		for (int j = 0; j < refList.getLength(); j++) {
			p.getParents().add(new IdRecord(refList.item(j).getTextContent()));
		}
		
		NodeList citList = e.getElementsByTagName("cit-id");
		for (int j = 0; j < citList.getLength(); j++) {
			p.getChilds().add(new IdRecord(citList.item(j).getTextContent()));
		}
		
		return p;
	}
	
	
	/**
	 * vrací obsah tagu ve stringu
	 * @param sTag - název tagu jehož hodnota je požadována
	 * @param element - element, ve kterém se tag nachází
	 * @return
	 */
	public static String getTagValue(String sTag, Element element) {
		if (element != null)
			return element.getElementsByTagName(sTag).item(0).getTextContent();
		
		return null;
	}
}
