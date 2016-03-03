package org.openlca.core.database;

import java.util.ArrayList;
import java.util.List;

public class Notifiable implements INotifiable {

	private List<IDatabaseListener> listeners = new ArrayList<>();

	@Override
	public void addListener(IDatabaseListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(IDatabaseListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void notifyInsert(Object object) {
		for (IDatabaseListener listener : listeners)
			listener.modelInserted(object);
	}

	@Override
	public void notifyUpdate(Object object) {
		for (IDatabaseListener listener : listeners)
			listener.modelUpdated(object);
	}

	@Override
	public void notifyDelete(Object object) {
		for (IDatabaseListener listener : listeners)
			listener.modelDeleted(object);
	}

}
