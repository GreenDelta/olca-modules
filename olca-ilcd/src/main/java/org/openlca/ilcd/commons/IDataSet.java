package org.openlca.ilcd.commons;

import java.io.Serializable;
import java.util.List;

public interface IDataSet extends Serializable {

	DataSetType getDataSetType();

	String getURI();

	String getUUID();

	String getVersion();

	List<Classification> getClassifications();

	List<LangString> getName();

}
