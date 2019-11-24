package cz.uhk.fim.citeviz.gui.components;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

public class AdvancedTableCellRenderer extends JTextArea implements TableCellRenderer {
	private static final long serialVersionUID = 1L;


	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

		this.setText(String.valueOf(value));
		this.setWrapStyleWord(true);
		this.setLineWrap(true);

		// nastaví se šíøka políèka podle šíøky sloupce tabulky
		setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);

		// nastaví se výška øádku tabulky podle potøeby
		if (table.getRowHeight(row) < getPreferredSize().height + 5){
			table.setRowHeight(row, getPreferredSize().height + 5);
		}

		// nastaví se rámeèek pøi vybrání øádku
		if (isSelected){
			setBorder(BorderFactory.createBevelBorder(0, new Color(99, 130, 191), new Color(99, 130, 191)));
		} else {
 			setBorder(null);
		}
		return this;
	}
}