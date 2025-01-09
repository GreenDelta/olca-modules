package org.openlca.io.smartepd;

import java.util.Objects;

import org.openlca.core.model.Epd;

public class SmartEpdReader {

	private final SmartEpd smartEpd;

	private SmartEpdReader(SmartEpd smartEpd) {
		this.smartEpd = Objects.requireNonNull(smartEpd);
	}

	public static SmartEpdReader of(SmartEpd smartEpd) {
		return new SmartEpdReader(smartEpd);
	}

	public Epd read() {
		var epd = new Epd();
		update(epd);
		return epd;
	}

	public void update(Epd epd) {
		// TODO
	}
}
