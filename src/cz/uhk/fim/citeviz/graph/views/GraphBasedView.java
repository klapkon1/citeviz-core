package cz.uhk.fim.citeviz.graph.views;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

import cz.uhk.fim.citeviz.graph.builder.ViewBuilder;
import cz.uhk.fim.citeviz.graph.categorizer.Categorizer;
import cz.uhk.fim.citeviz.graph.primitives.Graph;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.util.CiteVizUtils;
import cz.uhk.fim.citeviz.ws.connector.DataInterface;

public abstract class GraphBasedView implements View, ChangeListener, ItemListener{
	
	private Set<IdRecord> rootRecords = new LinkedHashSet<IdRecord>(1);
	
	private DataInterface dataInterface;
	
	private JSlider sliDepth;
	
	private JLabel lblDepth;
	
	private JCheckBox chcShowLabels;
	
	public static final int DEFAULT_DEPTH = 0;
	
	public GraphBasedView(Set<IdRecord> rootIds, DataInterface dataInterface){
		this.rootRecords = rootIds;
		this.dataInterface = dataInterface;
		
		sliDepth = new JSlider(0, 5, DEFAULT_DEPTH);
		sliDepth.addChangeListener(this);
		lblDepth = new JLabel(String.valueOf(sliDepth.getValue()));
		chcShowLabels = new JCheckBox(Localizer.getString("view.showLabels"));
		chcShowLabels.setSelected(true);
		chcShowLabels.addItemListener(this);
	}
	
	public GraphBasedView(IdRecord rootId, DataInterface dataInterface) {
		this(CiteVizUtils.asSet(rootId), dataInterface);
	}
	
	public void addRoot(IdRecord idRecord){
		rootRecords.add(idRecord);
	}
	
	public void addRoot(Set<IdRecord> idRecords){
		rootRecords.addAll(idRecords);
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public void removeRoot(IdRecord idRecord){
		if (rootRecords.size() > 1){
			rootRecords.remove(idRecord);
			getGraph().getNodes().remove(idRecord);			
		} else {
			throw new IllegalArgumentException("Could not remove root! At least one root must exist in view!");
		}
	}
	
	public Set<IdRecord> getRootRecords() {
		return rootRecords;
	}
	
	public abstract Graph getGraph();
	
	@Override
	public boolean isMultiRoot() {
		return true;
	}
	
	@Override
	public boolean viewDependedCamera(GL2 gl2, GLU glu) {
		return false;
	}
	
	@Override
	public boolean viewChanged() {
		return getGraph().graphChanged();
	}
	
	@Override
	public void setCategorizer(Categorizer<IdRecord, ?> categorizer){
		getGraph().setCategorizer(categorizer);
	}
	

	@Override
	public List<IdRecord> getDisplayRecords() {
		return getGraph().getDataElements();
	}	
	
	@Override
	public void preparePopupMenu(IdRecord record, JPopupMenu popRecordMenu) {
		getGraph().preparePopupMenu(record, popRecordMenu);
		popRecordMenu.addSeparator();
		
		JMenuItem rootRemove = new JMenuItem(Localizer.getString("graph.root.remove"));
		rootRemove.setEnabled(getRootRecords().contains(record));
		rootRemove.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				removeRoot(record);
			}
		});
		popRecordMenu.add(rootRemove);
	
		JMenuItem rootAdd = new JMenuItem(Localizer.getString("graph.root.add"));
		rootAdd.setEnabled(!getRootRecords().contains(record));
		rootAdd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				rootRecords.add(record);
				ViewBuilder.updateView(GraphBasedView.this, dataInterface, rootRecords);
			}
		});
		popRecordMenu.add(rootAdd);
	}
	
	public JLabel getLblDepth() {
		return lblDepth;
	}
	
	public JSlider getSliDepth() {
		return sliDepth;
	}
	
	public int getSelectedDepth() {
		return sliDepth.getValue();
	}
	
	public JCheckBox getChcShowLabels() {
		return chcShowLabels;
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == sliDepth && !((JSlider)e.getSource()).getValueIsAdjusting()){
			boolean newGraph = false;
			
			if (sliDepth.getValue() < Integer.valueOf(lblDepth.getText())){
				newGraph = true;
			}
			
			lblDepth.setText(String.valueOf(sliDepth.getValue()));
			
			onDepthChanged(dataInterface, newGraph);
		}	
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == chcShowLabels) {
			getGraph().setShowLabels(chcShowLabels.isSelected());
		}
	}
	
	protected abstract void onDepthChanged(DataInterface dataInterface, boolean newGraphNeeded);
}