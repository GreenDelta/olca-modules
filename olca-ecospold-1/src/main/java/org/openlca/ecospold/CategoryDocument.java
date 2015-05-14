package org.openlca.ecospold;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A EcoSpold 01 category document.
 */
@XmlRootElement(
		name = "categories",
		namespace = "http://www.EcoInvent.org/Categories")
@XmlAccessorType(XmlAccessType.FIELD)
public class CategoryDocument {

	@XmlElement(
			name = "category",
			namespace = "http://www.EcoInvent.org/Categories")
	private List<Category> categories = new ArrayList<>();

	public List<Category> getCategories() {
		return categories;
	}

}
