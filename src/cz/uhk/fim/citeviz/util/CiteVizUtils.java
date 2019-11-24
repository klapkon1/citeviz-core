package cz.uhk.fim.citeviz.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class CiteVizUtils {

	public static <T> Set<T> asSet(T object){
		Set<T> set = new HashSet<T>(1);
		set.add(object);
		return set;
	}
	
	public static <T> T asItem(Set<T> objects){
		if (objects.size() == 0){
			return null;
		} else if (objects.size() == 1){
			return objects.iterator().next();
		} else {
			throw new IllegalArgumentException("Unexpected size of set, expected 0 or 1 item, but got: " + objects.size());
		}
	}
	
	
	public static int convertRange(int oldMin, int oldMax, int newMin, int newMax, int oldValue){
		int oldRange = oldMax - oldMin;
		int newRange = newMax - newMin;
		
		if (oldRange == 0){
			return newMin;
		}
		
		return (int)(((oldValue - oldMin) / (float) oldRange) * newRange) + newMin;
	}
	
	public static float convertRange(float oldMin, float oldMax, float newMin, float newMax, float oldValue){
		float oldRange = oldMax - oldMin;
		float newRange = newMax - newMin;
		
		if (oldRange == 0){
			return newMin;
		}
		
		return (((oldValue - oldMin) / (float) oldRange) * newRange) + newMin;
	}

	public static <T> List<T> asList(T item) {
		List<T> list = new ArrayList<>(1);
		list.add(item);
		return list;
	}
	
	public static List<String> getLabelWrapped(String originalLabel, int lineLength, int maxLines){
		if (lineLength == 0) return new ArrayList<String>();
		
		int offset = 0, i;
		if (originalLabel.length() < lineLength) {
			return CiteVizUtils.asList(originalLabel);
		}
			
		List<String> labelWrapped = new ArrayList<>();
		for (i = lineLength; i < originalLabel.length() + lineLength; i += lineLength) {
			if (i >= originalLabel.length()){
				labelWrapped.add(originalLabel.substring(i - lineLength).trim());
				break;
			}
			while (!Character.isWhitespace(originalLabel.charAt(i - offset)) && offset < i) {
				offset++;
			}
			if (offset >= lineLength) 
				offset = 0;
				
			labelWrapped.add(originalLabel.substring(i - lineLength, i - offset).trim());
			
			i -= offset;
			offset = 0;
			
			if (labelWrapped.size() >= maxLines) {
				break;
			}
		}
	
		return labelWrapped;
	}
	
	public static class DoubleBox<A, B>{
		private A valueA;
		private B valueB;
		
		public DoubleBox(A valueA, B valueB) {
			this.valueA = valueA;
			this.valueB = valueB;
		}

		public A getValueA() {
			return valueA;
		}
		
		public B getValueB() {
			return valueB;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((valueA == null) ? 0 : valueA.hashCode());
			result = prime * result + ((valueB == null) ? 0 : valueB.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DoubleBox other = (DoubleBox) obj;
			if (valueA == null) {
				if (other.valueA != null)
					return false;
			} else if (!valueA.equals(other.valueA))
				return false;
			if (valueB == null) {
				if (other.valueB != null)
					return false;
			} else if (!valueB.equals(other.valueB))
				return false;
			return true;
		}
	}
}