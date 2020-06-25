package org.openlca.core.database;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.descriptors.Descriptor;

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
	public void notifyInsert(Descriptor descriptor) {
		for (IDatabaseListener listener : listeners)
			listener.modelInserted(descriptor);
	}

	@Override
	public void notifyUpdate(Descriptor descriptor) {
		for (IDatabaseListener listener : listeners)
			listener.modelUpdated(descriptor);
	}

	@Override
	public void notifyDelete(Descriptor descriptor) {
		for (IDatabaseListener listener : listeners)
			listener.modelDeleted(descriptor);
	}

}
