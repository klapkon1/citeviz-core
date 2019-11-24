package cz.uhk.fim.citeviz.graph.categorizer;

import java.awt.Color;

import javax.swing.JCheckBox;

import cz.uhk.fim.citeviz.graph.primitives.Colors;

/**
 * 
 * @author Ondrej Klapka
 *
 * @param <V> - value type
 */
public abstract class Category<V>{
	
	private Color color;
	
	private JCheckBox checkbox;
	
	private boolean isHidden = false;
	
	private int objectsCount;

	public Category(Color color) {
		super();
		this.color = color;
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setObjectsCount(int objectsCount) {
		this.objectsCount = objectsCount;
	}
	
	public int getObjectsCount() {
		return objectsCount;
	}
	
	public JCheckBox getCheckbox() {
		if (checkbox == null){
			checkbox = new JCheckBox(getLabel() + " (" + getObjectsCount() + ")"); 
			checkbox.setSelected(true);
			checkbox.setBackground(getColor());
			checkbox.setToolTipText(getLabel() + " (" + getObjectsCount() + ")");
			if (Colors.isColorDark(getColor())){
				checkbox.setForeground(Color.WHITE);
			}
		}
		
		return checkbox;
	}
	
	
	
	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}
	
	public boolean isSelected(){
		return getCheckbox().isSelected() && !isHidden;
	}
	
	public void setSelected(boolean selected) {
		getCheckbox().setSelected(selected);
	}
	
	public void setColor(Color color) {
		getCheckbox().setBackground(color);
		if (Colors.isColorDark(getColor())){
			getCheckbox().setForeground(Color.WHITE);
		}
		this.color = color;
	}
	
	public abstract String getLabel();
	
	public abstract boolean isInCategory(V val);
}