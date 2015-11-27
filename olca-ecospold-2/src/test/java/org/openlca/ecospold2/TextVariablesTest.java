package org.openlca.ecospold2;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

public class TextVariablesTest {

	@Test
	public void testTextVariables() throws Exception {
		InputStream is = getClass().getResourceAsStream("text_variables_test.spold");
		DataSet ds = EcoSpold2.readDataSet(is);
		String comment = ds.getActivity().getGeneralComment();
		Assert.assertFalse(comment.contains("plant {{location}} in {{time_period}}."));
		Assert.assertTrue(comment.contains("plant in Netherlands in 2012."));
	}
	
}
