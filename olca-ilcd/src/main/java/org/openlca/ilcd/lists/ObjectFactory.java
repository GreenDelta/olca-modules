package org.openlca.ilcd.lists;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.openlca.ilcd.lists package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

	private final static QName _ILCDLocations_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Locations", "ILCDLocations");
	private final static QName _CategorySystem_QNAME = new QName(
			"http://lca.jrc.it/ILCD/Categories", "CategorySystem");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: org.openlca.ilcd.lists
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link LocationList }
	 * 
	 */
	public LocationList createLocationList() {
		return new LocationList();
	}

	/**
	 * Create an instance of {@link Location }
	 * 
	 */
	public Location createLocation() {
		return new Location();
	}

	/**
	 * Create an instance of {@link CategorySystem }
	 * 
	 */
	public CategorySystem createCategorySystem() {
		return new CategorySystem();
	}

	/**
	 * Create an instance of {@link Category }
	 * 
	 */
	public Category createCategory() {
		return new Category();
	}

	/**
	 * Create an instance of {@link CategoryList }
	 * 
	 */
	public CategoryList createCategoryList() {
		return new CategoryList();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link LocationList }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Locations", name = "ILCDLocations")
	public JAXBElement<LocationList> createILCDLocations(LocationList value) {
		return new JAXBElement<>(_ILCDLocations_QNAME, LocationList.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link CategorySystem }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://lca.jrc.it/ILCD/Categories", name = "CategorySystem")
	public JAXBElement<CategorySystem> createCategorySystem(CategorySystem value) {
		return new JAXBElement<>(_CategorySystem_QNAME, CategorySystem.class,
				null, value);
	}

}
