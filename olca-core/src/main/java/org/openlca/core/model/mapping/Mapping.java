/*******************************************************************************
 * Copyright (c) 2007 - 2013 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDelta - initial API and implementation
 * www.greendelta.com tel.: +49 30 4849 6030 mail: gdtc@greendelta.com
 ******************************************************************************/
package org.openlca.core.model.mapping;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.mapping.content.MappingContent;

@Entity
@Table(name = "tbl_mappings")
public class Mapping extends AbstractEntity {

	@Column(name = "input")
	private boolean input;

	@Column(name = "model_type")
	private ModelType modelType;

	@Column(name = "format")
	private MapFormat format;

	@OneToOne
	@JoinColumn(name = "f_olca_content")
	private MappingContent olca_content;

	@OneToOne
	@JoinColumn(name = "f_content")
	private MappingContent content;

	public boolean isInput() {
		return input;
	}

	public void setInput(boolean input) {
		this.input = input;
	}

	public MapFormat getFormat() {
		return format;
	}

	public void setFormat(MapFormat format) {
		this.format = format;
	}

	public ModelType getModelType() {
		return modelType;
	}

	public void setModelType(ModelType modelType) {
		this.modelType = modelType;
	}

	public MappingContent getContent() {
		return content;
	}

	public void setContent(MappingContent content) {
		this.content = content;
	}

	public MappingContent getOlca_content() {
		return olca_content;
	}

	public void setOlca_content(MappingContent olca_content) {
		this.olca_content = olca_content;
	}

}
