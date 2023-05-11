package org.openlca.io.ecospold2.output;

public class EcoSpold2ExportConfig {

	static EcoSpold2ExportConfig DEFAULT = builder().build();
	final boolean uncutNames;
	final boolean distinguishWasteTreaments;

	private EcoSpold2ExportConfig(boolean uncutNames, boolean distinguishWasteTreaments) {
		this.uncutNames = uncutNames;
		this.distinguishWasteTreaments = distinguishWasteTreaments;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private boolean uncutNames;
		private boolean distinguishWasteTreaments;

		private Builder() {
		}
		
		public Builder uncutNames() {
			this.uncutNames = true;
			return this;
		}

		public Builder distinguishWasteTreaments() {
			this.distinguishWasteTreaments = true;
			return this;
		}

		public EcoSpold2ExportConfig build() {
			return new EcoSpold2ExportConfig(uncutNames, distinguishWasteTreaments);
		}

	}

}