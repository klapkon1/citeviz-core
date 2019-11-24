package cz.uhk.fim.citeviz.graph.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GDFParser {
	public static final char ODDELOVAC_RADKU = '\n';
	public static final char ODDELOVAC_SLOUPCU = ';';
	public static final char UVOZOVKA_DVOJITA = '"';
	public static final char UVOZOVKA_JEDNODUCHA = '\'';
	public static final char ODDELOVAC_SLOZENE_HODNOTY = ',';
	
	private Set<String> hlavicka;
	private List<String[]> data;
	
	public GDFParser(String retezec){
		hlavicka = new LinkedHashSet<String>();
		data = new ArrayList<String[]>();
		
		List<String> radky = rozdelRetezec(retezec, ODDELOVAC_RADKU, UVOZOVKA_DVOJITA);
		
		for(int i = 0; i < radky.size(); i++) {
			List<String> sloupce = trim(rozdelRetezec(radky.get(i), ODDELOVAC_SLOUPCU, UVOZOVKA_DVOJITA));
			if (i == 0){
				hlavicka.addAll(sloupce);
			}else{
				data.add(sloupce.toArray(new String[0]));
			}
		}
	}
	
	public static List<String> trim(List<String> hodnoty){
		for (int i = 0; i < hodnoty.size(); i++) {
			String hodnota = hodnoty.get(i);
			
			hodnota = hodnota.trim();
			if (hodnota.startsWith(String.valueOf(UVOZOVKA_DVOJITA)) 
			|| hodnota.startsWith(String.valueOf(UVOZOVKA_JEDNODUCHA))){
				hodnota = hodnota.substring(1);
			}
			
			if (hodnota.endsWith(String.valueOf(UVOZOVKA_DVOJITA)) 
			|| hodnota.endsWith(String.valueOf(UVOZOVKA_JEDNODUCHA))){
				hodnota = hodnota.substring(0, hodnota.length() - 1);
			}
			
			hodnota = hodnota.trim();
			
			hodnoty.set(i, hodnota);
		}
		
		return hodnoty;
	}
	
	public static List<String> rozdelRetezec(String retezec, char oddelovac, char uvozovka){
		List<String> casti = new ArrayList<String>();
		boolean vUvozovkach = false;
		int zacatek=0;
		for (int i = 0; i < retezec.length(); i++) {
			char aktualniZnak = retezec.charAt(i);
			
			if(aktualniZnak == uvozovka){
				vUvozovkach = !vUvozovkach;
			}
			if(aktualniZnak == oddelovac && !vUvozovkach){
				String cast = retezec.substring(zacatek, i);
				casti.add(cast);
				zacatek = i+1;
			}
		}
		
		casti.add(retezec.substring(zacatek, retezec.length()));
		
		return casti;
	}
	
	public int getPocetRadku(){
		return data.size();
	}
	
	public HashMap<String, String> getRadek(int index){
		HashMap<String, String> radek = new HashMap<String, String>();
		
		int i = 0;
		for (String klic : hlavicka) {
			radek.put(klic, data.get(index)[i]);
			i++;
		}
		
		return radek;
	}
	
}
