
package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.openlca.ilcd.commons.DataSetReference;


/**
 * <p>Java class for ComplementingProcessesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ComplementingProcessesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="referenceToComplementingProcess" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComplementingProcessesType", propOrder = {
    "complementingProcesses"
})
public class ComplementingProcesseList
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "referenceToComplementingProcess", required = true)
    protected List<DataSetReference> complementingProcesses;

    /**
     * Gets the value of the complementingProcesses property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the complementingProcesses property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComplementingProcesses().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataSetReference }
     * 
     * 
     */
    public List<DataSetReference> getComplementingProcesses() {
        if (complementingProcesses == null) {
            complementingProcesses = new ArrayList<>();
        }
        return this.complementingProcesses;
    }

}
