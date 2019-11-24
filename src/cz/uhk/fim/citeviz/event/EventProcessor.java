package cz.uhk.fim.citeviz.event;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class EventProcessor {

	private static Set<EventListener<Object>> listeners = new HashSet<EventListener<Object>>();
	
	public static void sendEvent(String eventName, Object eventData){
		Iterator<EventListener<Object>> iterator = listeners.iterator();
		
		while (iterator.hasNext()) {
			EventListener<Object> listener = iterator.next();
			
			if (listener.isEventForMe(eventName) && listener.accept(eventData)){
				listener.processEvent(eventData);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void registerListener(EventListener<?> listener){
		listeners.add((EventListener<Object>)listener);
	}
	
	public static void unregisterListener(EventListener<?> listener){
		listeners.remove(listener);
	}
}
