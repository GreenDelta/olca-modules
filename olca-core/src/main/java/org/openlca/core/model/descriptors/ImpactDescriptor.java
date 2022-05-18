package org.openlca.core.model.descriptors;

import org.openlca.core.model.Direction;
import org.openlca.core.model.ModelType;

/**
 * The descriptor class for impact assessment categories.
 */
public class ImpactDescriptor extends RootDescriptor {

	public String referenceUnit;
	public Direction direction;

	public ImpactDescriptor() {
		this.type = ModelType.IMPACT_CATEGORY;
	}

	@Override
	public ImpactDescriptor copy() {
		var copy = new ImpactDescriptor();
		copyFields(this, copy);
		copy.referenceUnit = referenceUnit;
		copy.direction = direction;
		return copy;
	}

	public static Builder create() {
		return new Builder(new ImpactDescriptor());
	}

	public static class Builder extends DescriptorBuilder<ImpactDescriptor> {

		private Builder(ImpactDescriptor descriptor) {
			super(descriptor);
		}

		public Builder referenceUnit(String referenceUnit) {
			descriptor.referenceUnit = referenceUnit;
			return this;
		}

		public Builder direction(Direction direction) {
			descriptor.direction = direction;
			return this;
		}
	}
}
