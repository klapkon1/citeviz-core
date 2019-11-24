package cz.uhk.fim.citeviz.graph.categorizer;

import java.awt.Color;
import java.util.List;

import cz.uhk.fim.citeviz.gui.components.Localizer;

public class OtherGroupCategory extends Category<Object>{

	private List<Category<Object>> otherCategories;
	
	public OtherGroupCategory(Color color, List<Category<Object>> otherCategories) {
		super(color);
	}

	@Override
	public String getLabel() {
		return Localizer.getString("categorizer.otherValue");
	}

	@Override
	public boolean isInCategory(Object val) {
		for (Category<Object> category : otherCategories) {
			if (category.isInCategory(val)) {
				return true;
			}
		}
		
		return false;
	}
}