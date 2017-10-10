package org.openlca.core.database;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.descriptors.BaseDescriptor;

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
	public void notifyInsert(BaseDescriptor descriptor) {
		for (IDatabaseListener listener : listeners)
			listener.modelInserted(descriptor);
	}

	@Override
	public void notifyUpdate(BaseDescriptor descriptor) {
		for (IDatabaseListener listener : listeners)
			listener.modelUpdated(descriptor);
	}

	@Override
	public void notifyDelete(BaseDescriptor descriptor) {
		for (IDatabaseListener listener : listeners)
			listener.modelDeleted(descriptor);
	}

}
