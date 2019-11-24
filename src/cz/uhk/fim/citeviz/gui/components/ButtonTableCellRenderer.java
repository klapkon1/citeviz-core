package cz.uhk.fim.citeviz.gui.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import cz.uhk.fim.citeviz.async.TaskManager;

public class ButtonTableCellRenderer extends JButton implements TableCellRenderer{

	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		setText("xx");
		
		addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Caled for ID:" + value);
				TaskManager.getInstance().stopTask((String)value);
			}
		});
		
		return this;
	}

}
