package cz.uhk.fim.citeviz.graph.categorizer;

import java.awt.Color;

public class IntervalCategory<V extends Number> extends Category<V>{
	private V fromVal;
	private V toVal;
	
	public IntervalCategory(Color color, V fromVal, V toVal) {
		super(color);
		this.fromVal = fromVal;
		this.toVal = toVal;
	}
	
	public V getFromVal() {
		return fromVal;
	}
	
	public V getToVal() {
		return toVal;
	}
	
	public void setFromVal(V fromVal) {
		this.fromVal = fromVal;
	}
	
	public void setToVal(V toVal) {
		this.toVal = toVal;
	}
	
	public boolean isInCategory(V val){
		return ((val.doubleValue() <= toVal.doubleValue()) && (val.doubleValue() >= fromVal.doubleValue()));
	}
	
	@Override
	public String getLabel(){
		if (fromVal.equals(toVal)){
			return getNumberAsString(fromVal);
		} else {
			return getNumberAsString(fromVal) + " - " + getNumberAsString(toVal);
		}
	}
	
	
	
	private String getNumberAsString(V number){
		if (number.doubleValue() == (int) number.doubleValue()) {
			return String.valueOf((int) number);
		} else {
			return String.valueOf(number);
		}
	}
}