package cz.uhk.fim.citeviz.graph.categorizer;

import java.awt.Color;

import cz.uhk.fim.citeviz.gui.components.Localizer;

public class UnknownCategory extends Category<Object> {

	public UnknownCategory(Color color) {
		super(color);
	}

	@Override
	public String getLabel() {
		return Localizer.getString("categorizer.unknownValue");
	}

	@Override
	public boolean isInCategory(Object val) {
		return val == null;
	}
}