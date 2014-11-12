package org.openlca.core.json;

public class WriterConfig {

	private boolean prettyPrinting;

	public static WriterConfig getDefault() {
		return new WriterConfig();
	}

	public void setPrettyPrinting(boolean prettyPrinting) {
		this.prettyPrinting = prettyPrinting;
	}

	public boolean isPrettyPrinting() {
		return prettyPrinting;
	}

}
