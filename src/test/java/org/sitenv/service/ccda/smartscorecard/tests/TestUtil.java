package org.sitenv.service.ccda.smartscorecard.tests;

import org.junit.Assert;
import org.sitenv.service.ccda.smartscorecard.util.ApplicationUtil;

public class TestUtil {

	public static void convertStackTraceToStringAndAssertFailWithIt(Exception e) {
		Assert.fail("The test failed due to the following exception:"
				+ System.lineSeparator()
				+ ApplicationUtil.convertStackTraceToString(e));
	}

}
