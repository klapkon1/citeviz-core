package cz.uhk.fim.citeviz.event;

public abstract class EventListener<D> {

	public abstract boolean isEventForMe(String eventName);
	
	public abstract void processEvent(D eventData);

	public abstract boolean accept(Object eventData);
	
	@Override
	public int hashCode() {
		return getClass().getName().hashCode();
	}
}
