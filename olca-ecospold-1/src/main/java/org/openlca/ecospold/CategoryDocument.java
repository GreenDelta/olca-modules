package org.openlca.ecospold;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

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
