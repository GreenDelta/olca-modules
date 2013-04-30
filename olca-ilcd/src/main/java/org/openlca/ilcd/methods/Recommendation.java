
package org.openlca.ilcd.methods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.FreeText;
import org.openlca.ilcd.commons.RecommendationLevel;


/**
 * <p>Java class for RecommendationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RecommendationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="referenceToEntity" type="{http://lca.jrc.it/ILCD/Common}GlobalReferenceType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="level" type="{http://lca.jrc.it/ILCD/Common}RecommendationLevelValues" minOccurs="0"/>
 *         &lt;element name="meaning" type="{http://lca.jrc.it/ILCD/Common}FTMultiLang" maxOccurs="100" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecommendationType", propOrder = {
    "referenceToEntity",
    "level",
    "meaning"
})
public class Recommendation
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    protected List<DataSetReference> referenceToEntity;
    protected RecommendationLevel level;
    protected List<FreeText> meaning;

    /**
     * Gets the value of the referenceToEntity property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the referenceToEntity property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferenceToEntity().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataSetReference }
     * 
     * 
     */
    public List<DataSetReference> getReferenceToEntity() {
        if (referenceToEntity == null) {
            referenceToEntity = new ArrayList<>();
        }
        return this.referenceToEntity;
    }

    /**
     * Gets the value of the level property.
     * 
     * @return
     *     possible object is
     *     {@link RecommendationLevel }
     *     
     */
    public RecommendationLevel getLevel() {
        return level;
    }

    /**
     * Sets the value of the level property.
     * 
     * @param value
     *     allowed object is
     *     {@link RecommendationLevel }
     *     
     */
    public void setLevel(RecommendationLevel value) {
        this.level = value;
    }

    /**
     * Gets the value of the meaning property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the meaning property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMeaning().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FreeText }
     * 
     * 
     */
    public List<FreeText> getMeaning() {
        if (meaning == null) {
            meaning = new ArrayList<>();
        }
        return this.meaning;
    }

}
