package org.openlca.io;

import com.google.common.eventbus.EventBus;

public interface FileImport extends Runnable {

	void setEventBus(EventBus eventBus);

	void cancel();

}
