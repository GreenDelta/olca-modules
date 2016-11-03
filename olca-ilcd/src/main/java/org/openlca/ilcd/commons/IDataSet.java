package org.openlca.ilcd.commons;

import java.io.Serializable;

public interface IDataSet extends Serializable {

	DataSetType getDataSetType();

	String getURI();

	String getUUID();

	String getVersion();

}
