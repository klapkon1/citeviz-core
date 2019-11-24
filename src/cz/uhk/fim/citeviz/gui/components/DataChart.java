package cz.uhk.fim.citeviz.gui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import cz.uhk.fim.citeviz.event.EventListener;
import cz.uhk.fim.citeviz.event.EventProcessor;
import cz.uhk.fim.citeviz.graph.engine.ObjectPicker;
import cz.uhk.fim.citeviz.graph.primitives.Colors;
import cz.uhk.fim.citeviz.model.IdRecord;

/**
 * Vykreslí jednoduchý histogram na základì hodnot pøedaných v konstruktoru
 * @author Ondøej Klapka
 *
 */
public abstract class DataChart<T> extends JTabbedPane{
	private static final long serialVersionUID = 1L;
	private List<T> values;
	private float widthRatio;
	private float heightRatio;
	private float max;
	private int zeroX;
	private int zeroY;
	private String captionX;
	private String captionY;
	private int mouseX = -1;
	private int mouseY = -1;
	private IdRecord outerSelection;
	private JLabel lblChart;
	private JTable tblData;
	
	
	public DataChart(List<T> values, String captionX, String captionY) {
		this.values = values;
		this.captionX = captionX;
		this.captionY = captionY;
		

		lblChart = new JLabel(){
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, getWidth(), getHeight());
				paintChart(g);
			}
		};
		
		
		add(Localizer.getString("chart.tabChart"), lblChart);
		tblData = new JTable(getTableModel());
		tblData.setRowSelectionAllowed(true);
		tblData.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		add(Localizer.getString("chart.tabTable"), new JScrollPane(tblData));	
		setTabPlacement(JTabbedPane.BOTTOM);

		lblChart.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				outerSelection = null;
				mouseX = e.getX();
				mouseY = e.getY();
				lblChart.repaint();
			}
		});
		
		lblChart.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				mouseX = -1;
				mouseY = -1;
				lblChart.repaint();
			}
		});
		
		
		ListSelectionModel selectionModel = tblData.getSelectionModel();

		selectionModel.addListSelectionListener(new ListSelectionListener() {
		    public void valueChanged(ListSelectionEvent e) {
		    	if (tblData.getSelectedRowCount() == 1){
					@SuppressWarnings("unchecked")
					T selectedObject = (T) tblData.getModel().getValueAt(tblData.getSelectedRow(), -1);
					onObjectSelect(selectedObject);
				
				}
		    }
		});
		
		
		EventProcessor.registerListener(new EventListener<IdRecord>() {
			@Override
			public boolean accept(Object eventData) {
				return eventData == null || eventData instanceof IdRecord;
			}

			@Override
			public boolean isEventForMe(String eventName) {
				return ObjectPicker.EVENT_ON_PICK.equals(eventName);
			}

			@Override
			public void processEvent(IdRecord eventData) {
				outerSelection = eventData;
				lblChart.repaint();
				selectRecordInTable();
			}
		});
	}
	 
	
	private AbstractTableModel getTableModel(){
		return new AbstractTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getColumnName(int column) {
				if (column == 1){
					return captionY;
				} else {
					return captionX;	
				}
			}
			
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				T object = values.get(rowIndex);
				
				if (columnIndex == 1){
					return extractValueFromObject(object);
				} else if (columnIndex == 0){
					return extractIdFromObject(object).getLongCaption();	
				} else {
					return object;
				}
			}
			
			@Override
			public int getRowCount() {
				return values.size();
			}
			
			@Override
			public int getColumnCount() {
				return 2;
			}
		};
	}
	
	private void selectRecordInTable() {
		
		for (int i = 0; i < values.size(); i++) {
			IdRecord id = extractIdFromObject(values.get(i));
			if (id != null && id.equals(outerSelection)){
				tblData.setRowSelectionInterval(i, i);
			}
		}
	}
	
	private void paintChart(Graphics g){
		if (values.size() == 0 || getWidth() < 20 || getHeight() < 20) return;
		
		zeroX = 35;
		zeroY = getHeight()-45;
		
		for (T val : values) {
			if (max < extractValueFromObject(val)){
				max = extractValueFromObject(val);
			}
		}
		
		heightRatio = (zeroY - 10)/(float)max;
		widthRatio = (getWidth() - zeroX - 2)/(float)values.size();
		
		g.setFont(new Font("Tahoma", Font.PLAIN, 9));
		
		drawAxis(g);

		
		T selectedObject = null;
		
		
		for (int i = 0; i < values.size(); i++) {
			if (drawColumnAndCheckFocus(g, (int)(zeroX + i * widthRatio) + 1, zeroY, values.get(i))){
				selectedObject = values.get(i);
			}
		}
		
		if (selectedObject != null) {
			lblChart.setToolTipText(extractIdFromObject(selectedObject).getLongCaption());
			//force tooltip show
			MouseEvent phantom = new MouseEvent(lblChart,
		         MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, mouseX, mouseY, 0, false);
		         ToolTipManager.sharedInstance().mouseMoved(phantom);
		} else {
			lblChart.setToolTipText(null);
		}
		
		
		if (outerSelection == null){
			onObjectSelect(selectedObject);
		}
		
		
		
		g.setColor(Color.black);
		g.drawString(captionX, getWidth() / 2 - 5 * captionX.length(), zeroY + 10);
		
		Graphics2D g2d = (Graphics2D)g;
	    AffineTransform fontAT = new AffineTransform();
	    Font theFont = g2d.getFont();
	    fontAT.rotate(- Math.PI / 2.0);
	    Font theDerivedFont = theFont.deriveFont(fontAT);
	    g2d.setFont(theDerivedFont);
	    g2d.drawString(captionY, 10, getHeight() / 2);
	}
	
	protected abstract void onObjectSelect(T selectedObject);

	private void drawAxis(Graphics g) {
		g.setColor(Color.black);
		//osa Y
		g.drawLine(zeroX, zeroY, zeroX, 0);
		
		//osa X
		g.drawLine(zeroX, zeroY, getWidth(), zeroY);
		
		//pokud je maximum rovno nule, není co kreslit na osu Y
		if (max == 0) return;
		
		//popisky Y
		float step = 1;
		int interval = 20;
		
		//výpoèet mìøítka hodnot na ose Y
		if (zeroY/(max/(float)step) < interval){
			step = 2;
			if (zeroY/(max/(float)step) < interval){
				step = 5;
				while (zeroY/(max/(float)step) < interval){
					step += 5;
				}	
			}
		} else{
			if (zeroY/(max/(float)step) > interval * 1.5f){
				step = 0.5f;
				if (zeroY/(max/(float)step) > interval * 1.5f){
					step = 0.2f;
					while (zeroY/(max/(float)step) > interval * 1.5f){
						step = step / 2f;
					}	
				}
			}
		};
		
		//zobrazení hodnot a dìlících èar
		for (float i = 0; i <= max + step / 2f; i += step) {
			
			float value = (Math.round(i * 1000) / 1000f);
			
			g.drawString(String.valueOf(value), 12, (int)(zeroY - i * heightRatio + 3));
			g.drawLine(zeroX - 2, 
					 	Math.round(Math.round(zeroY - i * heightRatio)),
					 	getWidth(), 
						Math.round(Math.round(zeroY - i * heightRatio)));
		}
	}


	private boolean drawColumnAndCheckFocus(Graphics g, int x, int y, T val) {		
		boolean focus = false;
		int height = Math.round(extractValueFromObject(val) * heightRatio) + 1;
		if ((mouseX > x && mouseX < (x + Math.round(widthRatio) + 1) && mouseY >= y - height && mouseY <= y) || extractIdFromObject(val).equals(outerSelection)){
			g.setColor(new Color(Colors.FOCUS[0], Colors.FOCUS[1], Colors.FOCUS[2]));
			focus = true;
		} else {
			g.setColor(getColorForColumn(val));
		}

		g.fillRect(x, y - height, Math.round(widthRatio) + 1, height);
		return focus;
	}
	
	protected Color getColorForColumn(T val){
		return Color.RED;
	}
	
	protected abstract float extractValueFromObject(T object);
	
	protected abstract IdRecord extractIdFromObject(T object);
}