package cz.uhk.fim.citeviz.graph.util;


import java.io.File;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import cz.uhk.fim.citeviz.graph.primitives.Edge;
import cz.uhk.fim.citeviz.graph.primitives.Graph;
import cz.uhk.fim.citeviz.graph.primitives.Node;
import cz.uhk.fim.citeviz.gui.components.Localizer;


/**
 * Tøída pokrývající proces naètení a uložení grafu do formátu GDF
 * @author Ondøej Klapka
 *
 */
public class GraphExport {
	
	public static void exportGraph(Graph g){
		PrintWriter pw = null;
		
		try {
			 JFileChooser fc = new JFileChooser();	
			 FileNameExtensionFilter filter = new FileNameExtensionFilter("GDF soubor (*.gdf)", "gdf");
			 fc.setFileFilter(filter);
			 if(fc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
				 return;   
			  };
			  
			 String soubor = fc.getSelectedFile().getAbsolutePath();
			 if (!soubor.endsWith(".gdf")){
				 soubor += ".gdf";
			 }
			 pw = new PrintWriter(new File(soubor));
			 
			 
			 pw.append("nodedef>name,label varchar(300),x DOUBLE,y DOUBLE\r\n");
			 //výpis vrcholù
			 for (Node<?> node : g.getNodes()) {
				 pw.append(String.valueOf(node.getData().getNumericId()));
				 pw.append(",\"");
				 pw.append(node.getData().getLongCaption());
				 pw.append("\",");
				 pw.append(String.valueOf(node.getX()));
				 pw.append(",");
				 pw.append(String.valueOf(node.getY()));
				 pw.append("\r\n");
			 }
			
			 pw.append("edgedef>node1,node2\r\n");
			 
			 for (Edge<?, ?> edge : g.getEdges()) {
				pw.append(String.valueOf(edge.getFrom().getData().getNumericId()));
				pw.append(",");
				pw.append(String.valueOf(edge.getTo().getData().getNumericId()));
				pw.append("\r\n");
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Graf se nepodaøilo uložit:\n" + e.getLocalizedMessage(), "Chyba ukládání", JOptionPane.ERROR_MESSAGE);
		} finally {
			if (pw != null){
				pw.close();
			}
		}
		
	}
	/**
	 * naète graf ze souboru GDF
	 * získání objektové podoby grafu se provádí pomocí metod
	 * getVrcholy a getHrany
	 */
	public static Graph importGraph(){
		Graph g = new Graph();
		
		JFileChooser fc = new JFileChooser();			 
		FileNameExtensionFilter filter = new FileNameExtensionFilter(Localizer.getString("FileChooser.filter.title"), "gdf");
		fc.setFileFilter(filter);
		if(fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
			 return null;   
		};
		
		return g;
	}
	
	//LOCALIZE DIALOG
	static {
		UIManager.put("FileChooser.openDialogTitleText","Otevøít graf");
		UIManager.put("FileChooser.saveDialogTitleText","Uložit graf");
		
		UIManager.put("FileChooser.lookInLabelText", "Otevøít z:");
		UIManager.put("FileChooser.saveInLabelText", "Uložit do:");
		
		UIManager.put("FileChooser.openButtonText", "Otevøít");
		UIManager.put("FileChooser.openButtonToolTipText", "Otevøít vybraný soubor");
		UIManager.put("FileChooser.saveButtonText", "Uložit");
		UIManager.put("FileChooser.saveButtonToolTipText", "Uložit do vybraného souboru");
		UIManager.put("FileChooser.cancelButtonText", "Storno");
		UIManager.put("FileChooser.cancelButtonToolTipText", "Zavøít dialog");
		

		UIManager.put("FileChooser.upFolderToolTipText", "O složku výše"); 
		UIManager.put("FileChooser.upFolderAccessibleName", "O složku výše"); 
		
        UIManager.put("FileChooser.newFolderToolTipText","Vytvoøit novou složku");
        UIManager.put("FileChooser.newFolderAccessibleName", "Vytvoøit novou složku"); 

		UIManager.put("FileChooser.listViewButtonToolTipText", "Zobrazit seznam"); 
		UIManager.put("FileChooser.listViewButtonAccessibleName", "Zobrazit seznam"); 
		
	    UIManager.put("FileChooser.detailsViewButtonToolTipText", "Zobrazit detaily");
		UIManager.put("FileChooser.detailsViewButtonAccessibleName", "Zobrazit detaily");
		
		UIManager.put("FileChooser.filesOfTypeLabelText", "Typ souboru");
		UIManager.put("FileChooser.fileNameLabelText", "Název souboru");
	}
}