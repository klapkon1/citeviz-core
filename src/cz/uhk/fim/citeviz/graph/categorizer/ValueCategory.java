package cz.uhk.fim.citeviz.graph.categorizer;

import java.awt.Color;
import java.util.Set;

public class ValueCategory extends Category<Object>{

	private Object value;
	
	public ValueCategory(Color color, Object value) {
		super(color);
		this.value = value;
	}

	@Override
	public String getLabel() {
		return value.toString();
	}

	@Override
	public boolean isInCategory(Object val) {
		if (val instanceof Set){
			return ((Set<?>) val).contains(value);
		}
		
		return value.equals(val);
	}
}