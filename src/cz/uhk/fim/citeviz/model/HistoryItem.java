package cz.uhk.fim.citeviz.model;

import cz.uhk.fim.citeviz.graph.engine.EyePosition;
import cz.uhk.fim.citeviz.graph.views.View;

public class HistoryItem {
	private View view;
	
	private EyePosition eyePosition;

	public HistoryItem(View view, EyePosition eyePosition) {
		super();
		this.view = view;
		this.eyePosition = eyePosition;
	}

	public View getView() {
		return view;
	}

	public EyePosition getEyePosition() {
		return eyePosition;
	}
}
