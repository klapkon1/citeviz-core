package cz.uhk.fim.citeviz.graph.categorizer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cz.uhk.fim.citeviz.graph.builder.ToolTipBuilder;
import cz.uhk.fim.citeviz.graph.primitives.Colors;
import cz.uhk.fim.citeviz.graph.primitives.Graph;
import cz.uhk.fim.citeviz.graph.primitives.Node;
import cz.uhk.fim.citeviz.gui.NodeSettings;
import cz.uhk.fim.citeviz.gui.components.DataChart;
import cz.uhk.fim.citeviz.gui.components.Localizer;
import cz.uhk.fim.citeviz.gui.components.WordCloudPanel;
import cz.uhk.fim.citeviz.gui.components.WrapLayout;
import cz.uhk.fim.citeviz.model.IdRecord;

/**
 * 
 * @author ondra
 *
 * @param <T> - type of categorized objects
 * @param <V> - type of categorized value
 */
public abstract class Categorizer<T, V> implements ActionListener{

	private List<T> objects;
	
	private List<Category<V>> categories;
	
	private int min;
	
	private int max;
	
	private JComponent pnlLegend;

	private JMenuItem btnStats;
	
	private JMenuItem btnSelectAll;
	
	private JMenuItem btnUnselectAll;
	
	private JMenuItem btnStatsSelected;
	
	private JMenuItem btnSetColor;
	
	private JMenuItem btnWordCloud;
	
	private JMenuItem btnCategoryNodesSettings;
	
	private Category<V> lastSelectedCategory;
	
	private Graph graph;
	
	public Categorizer(List<T> objects, JComponent pnlLegend, Graph graph) {
		this.objects = objects;
		this.pnlLegend = pnlLegend;
		this.graph = graph;
		this.categories = new ArrayList<Category<V>>();
		computeCategories();
		updatePnlLegedPopupMenu();
	}

	public void setObjects(List<T> objects) {
		this.objects = objects;
		computeCategories();
		updatePnlLegedPopupMenu();
	}
	
	public abstract V extractValueFromOjbject(T object);
	
	public Color getColorForObject(T object){
		V value = extractValueFromOjbject(object);
		
		Category<V> categorySelected = null;
		
		for (Category<V> category : categories) {
			if (category.isInCategory(value) && category.isSelected()) {
				if (categorySelected != null){
					return categorySelected.getColor(); //objekt ma jiz vybrany nejmene 2 kategorie - vrati se prvni kategorie
				} else {
					categorySelected = category;
				}
				
			}
		}
		
		return categorySelected != null ? categorySelected.getColor() : null;
	}
	
	
	
	public boolean isCategorySelected(T object){
		for (Category<V> category : categories) {
			if (category.isInCategory(extractValueFromOjbject(object)) && category.isSelected()){
				return true;
			}
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public void computeCategories(){
		categories.clear();
		if (objects == null || objects.isEmpty()){
			return;
		}
		
		
		if (extractValueFromOjbject(objects.get(0)) instanceof Number){
			commputeMinAndMax();
			//výpoèet poètu kategorií
			int catCount = 1 + (int)(3.3 * Math.log(objects.size()));
			if (catCount > 25) catCount = 25;
			
			//definování kategorií
			int interval = (max - min) / catCount;
			if (interval < 1){
				interval = 1;
			}
			
			for (int i = min; i <= max; i += interval) {
				float colorWeight = Math.min(categories.size()/(float)catCount, 1);
				Color color = new Color(1 - colorWeight, colorWeight, 0);
				categories.add((Category<V>)new IntervalCategory<>(color, i, (i + interval - 1)));
			}	
			//upravení poslední kategorie - musí konèit maximem
			if (!categories.isEmpty()){
				((IntervalCategory<Integer>)categories.get(categories.size() - 1)).setToVal(max);
			}
		} else if (extractValueFromOjbject(objects.get(0)) instanceof Set){
			Set<V> uniqueValues = new HashSet<V>();
			
			boolean nullValue = false;
			
			for (T object : objects) {
				Object value = extractValueFromOjbject(object);
				
				if (value instanceof Set){
					uniqueValues.addAll((Set<V>) value);
				} else {
					nullValue = true;
				}
			}
			
			int counter = 0;
			for (V value : uniqueValues) {
				categories.add((Category<V>)new ValueCategory(Colors.generateColorFromPallette(uniqueValues.size() + (nullValue ? 1 : 0), counter), value));
				counter++;
			}
			
			if (nullValue){
				categories.add((Category<V>)new UnknownCategory(Colors.generateColorFromPallette(uniqueValues.size(), counter)));
			}
		}
		
		Collections.sort(categories, new Comparator<Category<V>>() {

			@Override
			public int compare(Category<V> o1, Category<V> o2) {
				if (o1 instanceof UnknownCategory) {
					return 1;
				} else if (o2 instanceof UnknownCategory) {
					return -1;
				} else if (o1 instanceof IntervalCategory && o2 instanceof IntervalCategory) {
					return ((IntervalCategory<?>)o1).getFromVal().intValue() - ((IntervalCategory<?>)o2).getFromVal().intValue();
				}
				
				return o1.getLabel().compareTo(o2.getLabel());
			}

		
			
		});
		
		for (T object: objects) {
			for (Category<V> category : categories) {
				if (category.isInCategory(extractValueFromOjbject(object))){
					category.setObjectsCount(category.getObjectsCount() + 1);
				}
			}
		}
		
		
	}
	
	private void commputeMinAndMax() {
		min = (int)extractValueFromOjbject(objects.get(0));
		max = min;
		
		for (T object : objects) {
			int current = (int)extractValueFromOjbject(object);
			if (current > max){
				max = current;
			}
			
			if (current < min){
				min = current;
			}
		}
	}
	
	
	public void showLegend() {
		pnlLegend.setLayout(new WrapLayout(WrapLayout.CENTER));
		pnlLegend.removeAll();
		pnlLegend.add(new JLabel(Localizer.getString("categorizer.legend")));
		
		for (Category<V> category : categories) {
			JCheckBox checkbox = category.getCheckbox();
			pnlLegend.add(checkbox);
		}
				
		pnlLegend.revalidate();
		pnlLegend.repaint();
		pnlLegend.setVisible(true);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnStats){
			JFrame dialog = new JFrame();
			
			dialog.add(getCategoriesChart(categories));
			dialog.setTitle(Localizer.getString("categorizer.stats"));
			dialog.setSize(500, 400);
			dialog.setVisible(true);
		}
		
		if (e.getSource() == btnStatsSelected){
			JFrame dialog = new JFrame();
			
			List<Category<V>> categoriesToShow = new ArrayList<Category<V>>();
			
			for (Category<V> category : categories) {
				if (category.getCheckbox().isSelected()){
					categoriesToShow.add(category);
				}
			}
			
			dialog.add(getCategoriesChart(categoriesToShow));
			dialog.setTitle(Localizer.getString("categorizer.stats"));
			dialog.setSize(500, 400);
			dialog.setVisible(true);
		}
		
		if (e.getSource() == btnSelectAll){
			for (Category<V> category : categories) {
				category.getCheckbox().setSelected(true);
			}
		}
		
		if (e.getSource() == btnUnselectAll){
			for (Category<V> category : categories) {
				category.getCheckbox().setSelected(false);
			}
		}
		
		if (e.getSource() == btnCategoryNodesSettings) {
			List<Node<?>> nodesInCategory = new ArrayList<>();
			
			for (Node<?> node : graph.getNodes()) {
				if (lastSelectedCategory.isInCategory(extractValueFromOjbject((T)node.getData()))) {
					nodesInCategory.add(node);
				}
			}
			
			NodeSettings dlgNodeSettings = new NodeSettings(nodesInCategory);
			dlgNodeSettings.setVisible(true);
			lastSelectedCategory.setColor(dlgNodeSettings.getLastSelectedColor());
		}
		
		if (e.getSource() == btnSetColor){
			JDialog dlgColor = new JDialog();
			dlgColor.setModal(true);
			JColorChooser cchCategory = new JColorChooser(lastSelectedCategory.getColor());
			cchCategory.getSelectionModel().addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					lastSelectedCategory.setColor(cchCategory.getColor());
					lastSelectedCategory = null;
					dlgColor.setVisible(false);
				}
			});
			dlgColor.add(cchCategory);
			dlgColor.setAlwaysOnTop(true);
			dlgColor.pack();
			dlgColor.setTitle(Localizer.getString("categorizer.category.setColor"));
			dlgColor.setVisible(true);
		}
		
		if (e.getSource() == btnWordCloud) {
			JDialog dlgWordCloud = new JDialog();
			dlgWordCloud.setModal(true);
			dlgWordCloud.setAlwaysOnTop(true);
			dlgWordCloud.setTitle(Localizer.getString("wordCloud.title"));
			WordCloudPanel wordCloudPanel = new WordCloudPanel(categories);
			dlgWordCloud.add(wordCloudPanel);
			dlgWordCloud.setSize(wordCloudPanel.getSize());
			dlgWordCloud.setVisible(true);
		}
	}
	
	
	private DataChart<Category<V>> getCategoriesChart(List<Category<V>> categoriesToShow) {
		
		Collections.sort(categoriesToShow, new Comparator<Category<V>>() {

			@Override
			public int compare(Category<V> o1, Category<V> o2) {
				return o1.getObjectsCount() - o2.getObjectsCount();
			}
		});
		
		return new DataChart<Category<V>>(categoriesToShow, Localizer.getString("categorizer.stats.axis.X"), Localizer.getString("categorizer.stats.axis.Y")) {
			private static final long serialVersionUID = 1L;

			@Override
			protected float extractValueFromObject(Category<V> category) {
				int counter = 0;
				
				for (T obj : objects) {
					if (category.isInCategory(extractValueFromOjbject(obj))){
						counter++;
					}
				}
				
				return counter;
			}
			
			@Override
			protected void onObjectSelect(Category<V> selectedObject) {
				if (selectedObject != null){
					createTooltip(selectedObject);
					for (Category<V> category : categoriesToShow) {
						if (selectedObject != category){
							category.setHidden(true);
						} else {
							category.setHidden(false);
						}
					}
				} else {
					setToolTipText(null);
					for (Category<V> category : categoriesToShow) {
						category.setHidden(false);
					}
				}
			}

			private void createTooltip(Category<V> selectedObject) {
				ToolTipBuilder builder = new ToolTipBuilder();
				builder.beginHTMLAutosized();
				builder.beginBold();
				builder.append(Localizer.getString("categorizer.stats.category"));
				builder.append(" ");
				builder.append(selectedObject.getLabel());
				builder.endBold();
				builder.newLine();
				builder.append(Localizer.getString("categorizer.stats.objectsCount"));
				builder.append(" ");
				builder.append(String.valueOf((int)extractValueFromObject(selectedObject)));
				builder.endHTML();
				setToolTipText(builder.toString());
			}
			
			@Override
			protected IdRecord extractIdFromObject(Category<V> object) {
				return new IdRecord(object.getLabel());
			}
			
			@Override
			protected Color getColorForColumn(Category<V> val) {
				return val.getColor();
			}
		};
	}
	
	private void updatePnlLegedPopupMenu() {
		if (btnStats == null){
			btnStats = new JMenuItem(Localizer.getString("categorizer.stats"));
			btnStats.addActionListener(this);
		}
		
		if (btnStatsSelected == null){
			btnStatsSelected = new JMenuItem(Localizer.getString("categorizer.statsSelected"));
			btnStatsSelected.addActionListener(this);
		}
		
		if (btnSelectAll == null){
			btnSelectAll = new JMenuItem(Localizer.getString("categorizer.selectAll"));
			btnSelectAll.addActionListener(this);
		}
		
		if (btnUnselectAll == null){
			btnUnselectAll = new JMenuItem(Localizer.getString("categorizer.unselectAll"));
			btnUnselectAll.addActionListener(this);
		}
		
		if (btnWordCloud == null) {
			btnWordCloud = new JMenuItem(Localizer.getString("categorizer.wordCloud"));
			btnWordCloud.addActionListener(this);
		}
		
		if (btnSetColor == null){
			btnSetColor = new JMenuItem(Localizer.getString("categorizer.category.setColor"));
			btnSetColor.addActionListener(this);
		}
		
		if (btnCategoryNodesSettings == null && graph != null) {
			btnCategoryNodesSettings = new JMenuItem(Localizer.getString("categorizer.categoryNodes.settings"));
			btnCategoryNodesSettings.addActionListener(this);
		}
		
		for (MouseListener listener : pnlLegend.getMouseListeners()) {
			pnlLegend.removeMouseListener(listener);
		}
		
		MouseListener listener = new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				
				if (e.getButton() == MouseEvent.BUTTON3){
					JPopupMenu popMenu = new JPopupMenu();
					popMenu.add(btnStats);
					popMenu.add(btnStatsSelected);
					
					if (e.getSource() instanceof JCheckBox) {
						popMenu.add(btnSetColor);
						for (Category<V> category : categories) {
							if (category.getCheckbox() == e.getSource()){
								lastSelectedCategory = category;
								break;
							}
							
						}
					}
					
					popMenu.addSeparator();
					popMenu.add(btnSelectAll);
					popMenu.add(btnUnselectAll);
					popMenu.add(btnWordCloud);
					if (graph != null) {
						popMenu.add(btnCategoryNodesSettings);
					}
					popMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		};
		
		
		pnlLegend.addMouseListener(listener);
		for (Category<V> category : categories) {
			category.getCheckbox().addMouseListener(listener);
		}
	}
}