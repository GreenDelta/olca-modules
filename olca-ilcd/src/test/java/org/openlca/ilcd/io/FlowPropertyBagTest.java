package org.openlca.ilcd.io;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.openlca.ilcd.commons.Category;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.util.FlowPropertyBag;

public class FlowPropertyBagTest {

	private FlowPropertyBag bag;

	@Before
	public void setUp() throws Exception {
		try (InputStream stream = this.getClass().getResourceAsStream(
				"flowproperty.xml")) {
			XmlBinder binder = new XmlBinder();
			FlowProperty group = binder.fromStream(FlowProperty.class, stream);
			this.bag = new FlowPropertyBag(group, "en");
		}
	}

	@Test
	public void testGetId() {
		assertEquals("93a60a56-a3c8-14da-a746-0800200c9a66", bag.getId());
	}

	@Test
	public void testGetName() {
		assertEquals("Gross calorific value", bag.getName());
	}

	@Test
	public void testGetClasses() {
		Category clazz = bag.getSortedClasses().get(0);
		assertEquals(0, clazz.level);
		assertEquals("Technical flow properties", clazz.value.trim());
	}

	@Test
	public void testGetUnitGroupReference() {
		Ref ref = bag.getUnitGroupReference();
		assertEquals("93a60a57-a3c8-11da-a746-0800200c9a66", ref.uuid);
	}

}
