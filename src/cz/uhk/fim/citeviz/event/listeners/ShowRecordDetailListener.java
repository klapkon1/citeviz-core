package cz.uhk.fim.citeviz.event.listeners;

import cz.uhk.fim.citeviz.event.EventListener;
import cz.uhk.fim.citeviz.gui.RecordDetailPanel;
import cz.uhk.fim.citeviz.model.IdRecord;
import cz.uhk.fim.citeviz.ws.connector.DataInterface;

public class ShowRecordDetailListener extends EventListener<IdRecord>{

	private DataInterface dataInterface;
	
	public static final String EVENT_NAME = "showRecordDetail";
	
	public ShowRecordDetailListener(DataInterface dataInterface) {
		this.dataInterface = dataInterface;
	}

	@Override
	public boolean isEventForMe(String eventName) {
		return EVENT_NAME.equals(eventName);
	}

	@Override
	public void processEvent(IdRecord eventData) {
		new RecordDetailPanel(eventData, dataInterface);
	}

	@Override
	public boolean accept(Object eventData) {
		return eventData instanceof IdRecord;
	}
}