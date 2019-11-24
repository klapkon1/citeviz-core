package cz.uhk.fim.citeviz.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.RectangleBackground;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.palette.ColorPalette;

import cz.uhk.fim.citeviz.graph.categorizer.Category;

public class WordCloudPanel extends JPanel{
	
	private static final long serialVersionUID = 1L;

	public WordCloudPanel(List<? extends Category<?>> categories) {
		int sizeRatio = (int) Math.sqrt(categories.size());
		
		Dimension dimension = new Dimension(sizeRatio * 80, sizeRatio * 50);
		WordCloud wordCloud = buildWordCloud(categories, dimension);
		add(new JLabel(new ImageIcon(wordCloud.getBufferedImage())));
		setSize(dimension);
	}

	private WordCloud buildWordCloud(List<? extends Category<?>> categories, Dimension dimension) {
		//clone and sort categories
		categories = new ArrayList<>(categories);
		Collections.sort(categories, new Comparator<Category<?>>() {

			@Override
			public int compare(Category<?> o1, Category<?> o2) {
				return o2.getObjectsCount() - o1.getObjectsCount();
			}
		});
		
		List<WordFrequency> wordFrequencies = new ArrayList<>(categories.size());
		List<Color> colors = new ArrayList<>(categories.size());
		
		for (Category<?> category : categories) {
			wordFrequencies.add(new WordFrequency(category.getLabel(), category.getObjectsCount()));
			colors.add(category.getColor());
		}
		
		WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
		wordCloud.setPadding(5);
		wordCloud.setBackground(new RectangleBackground(dimension));
		wordCloud.setBackgroundColor(Color.WHITE);
		wordCloud.setColorPalette(new ColorPalette(colors));
		wordCloud.setFontScalar(new LinearFontScalar(10, 40));
		wordCloud.build(wordFrequencies);

		return wordCloud;
	}
}