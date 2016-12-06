package org.openlca.ecospold2;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

public class TextVariablesTest {

	@Test
	public void testTextVariables() throws Exception {
		InputStream is = getClass().getResourceAsStream("text_variables_test.spold");
		DataSet ds = EcoSpold2.readDataSet(is);
		String comment = ds.activity.generalComment;
		Assert.assertFalse(comment.contains("test {{var1}} and {{var2}}."));
		Assert.assertTrue(comment.contains("test val1 and val2."));
	}
	
}
