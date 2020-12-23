package de.elbosso;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class AppTest
{
	static final Logger logger = LogManager.getLogger(AppTest.class);
	@Test
	public void testAlwaysSuccessful()
	{
	}
	@org.junit.Ignore
	@org.junit.Test
	public void testFail()
	{
		org.junit.Assert.fail("TestExample");
	}
}