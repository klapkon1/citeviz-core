package cz.uhk.fim.citeviz.graph.alg.cluster;

import cz.uhk.fim.citeviz.gui.components.Localizer;

public enum ClusteringType {
	STRUCTURAL,
	KEYWORDS;
	
	public String toString() {
		return Localizer.localizeEnum(getClass(), name());	
	}
}
