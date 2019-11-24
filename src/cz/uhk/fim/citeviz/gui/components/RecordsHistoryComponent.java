package cz.uhk.fim.citeviz.gui.components;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import cz.uhk.fim.citeviz.graph.views.View;

public class RecordsHistoryComponent {

	private List<View> history = new ArrayList<>();
	
	

	public void storeViewToHistory(View view){
		history.add(view);
	}
	
	public void loadViewFromHistory(){
		
	}
	
	public JPanel renderHistoryTable(){
		return new JPanel();
	}
}