package cz.uhk.fim.citeviz.event.listeners;

import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import cz.uhk.fim.citeviz.event.EventListener;
import cz.uhk.fim.citeviz.graph.builder.ViewBuilder;
import cz.uhk.fim.citeviz.graph.engine.Renderer;
import cz.uhk.fim.citeviz.graph.views.NonGraphicView;
import cz.uhk.fim.citeviz.graph.views.View;
import cz.uhk.fim.citeviz.gui.Main;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.model.DataType;
import cz.uhk.fim.citeviz.model.HistoryItem;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.model.ViewType;
import cz.uhk.fim.citeviz.ws.connector.DataInterface;

public abstract class RootRecordSelectListener extends EventListener<Set<IdRecord>> {

	public static final String EVENT_NAME = "rootRecordSelect";
	
	private Main mainWin;
	
	private Renderer renderer;

	private DataInterface dataInterface;

	private JComboBox<DataType> cboData;

	private JComboBox<ViewType> cboViewPapers;

	private JComboBox<ViewType> cboViewAuthors;
	
	
	public RootRecordSelectListener(Main mainWin, Renderer renderer, DataInterface dataInterface, JComboBox<DataType> cboData, JComboBox<ViewType> cboViewPapers, JComboBox<ViewType> cboViewAuthors) {
		this.mainWin = mainWin;
		this.renderer = renderer;
		this.dataInterface = dataInterface;
		this.cboData = cboData;
		this.cboViewPapers = cboViewPapers;
		this.cboViewAuthors = cboViewAuthors;
	}

	@Override
	public boolean isEventForMe(String eventName) {
		return EVENT_NAME.equals(eventName);
	}

	@Override
	public void processEvent(Set<IdRecord> records) {
		ViewType selectedViewType = DataType.PAPERS.equals(cboData.getSelectedItem()) ? (ViewType) cboViewPapers.getSelectedItem() : (ViewType) cboViewAuthors.getSelectedItem();
		
		
		if (renderer.getView() != null && renderer.getView().isMultiRoot() && renderer.getView().getViewType().equals(selectedViewType)) {
			String[] buttons = {Localizer.getString("view.multiroot.yes"), Localizer.getString("view.multiroot.no")};

			int answer = JOptionPane.showOptionDialog(null,
					Localizer.getString("view.multiroot"),
					Localizer.getString("view.multiroot.title"),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, buttons, buttons[1]);
			if (answer == 0) {
				ViewBuilder.updateView(renderer.getView(), dataInterface, records);
				return;
			}

		}
		
		addItemToHistory(new HistoryItem(renderer.getView(), renderer.getEyePosition()));

		View view = ViewBuilder.createView(selectedViewType, dataInterface, records);
		
		if (view instanceof NonGraphicView){
			mainWin.switchView((NonGraphicView)view);
		} else {
			renderer.setView(view);
			mainWin.switchView(null);
		}
		
		
	}

	@Override
	public boolean accept(Object eventData) {
		return eventData instanceof Set;
	}
	
	protected abstract void addItemToHistory(HistoryItem item);
}