package org.openlca.core.model.descriptors;

import java.util.Objects;

public class DescriptorBuilder<T extends Descriptor>{

	protected final T descriptor;

	public DescriptorBuilder(T descriptor) {
		this.descriptor = Objects.requireNonNull(descriptor);
	}

	public DescriptorBuilder<T> id(long id) {
		descriptor.id = id;
		return this;
	}

	public DescriptorBuilder<T> refId(String refId) {
		descriptor.refId = refId;
		return this;
	}

	public DescriptorBuilder<T> name(String name) {
		descriptor.name = name;
		return this;
	}

	public DescriptorBuilder<T> description(String description) {
		descriptor.description = description;
		return this;
	}

	public DescriptorBuilder<T> version(long version) {
		descriptor.version = version;
		return this;
	}

	public DescriptorBuilder<T> lastChange(long lastChange) {
		descriptor.lastChange = lastChange;
		return this;
	}

	public DescriptorBuilder<T> library(String library) {
		descriptor.library = library;
		return this;
	}

	public DescriptorBuilder<T> tags(String tags) {
		descriptor.tags = tags;
		return this;
	}

	public T get() {
		return descriptor;
	}
}
