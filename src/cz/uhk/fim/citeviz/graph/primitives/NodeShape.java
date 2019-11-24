package cz.uhk.fim.citeviz.graph.primitives;

import cz.uhk.fim.citeviz.gui.components.Localizer;

public enum NodeShape {
	CIRCLE,
	RECTANGLE,
	TRIANGLE,
	DIAMOND;
	
	public String toString() {
		return Localizer.localizeEnum(getClass(), name());	
	}
}
