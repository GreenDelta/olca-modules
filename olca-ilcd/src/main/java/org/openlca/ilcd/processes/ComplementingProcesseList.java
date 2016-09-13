
package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.openlca.ilcd.commons.DataSetReference;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComplementingProcessesType", propOrder = {
		"complementingProcesses"
})
public class ComplementingProcesseList implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlElement(name = "referenceToComplementingProcess", required = true)
	public final List<DataSetReference> complementingProcesses = new ArrayList<>();

}
