package com.extinctionman;

import java.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SoulLogExporterTest
{
	@Test
	public void formatsReadableTabSeparatedLog()
	{
		String result = SoulLogExporter.format(Arrays.asList(
			new SoulProgress("Hellhound", 1),
			new SoulProgress("Goblin", 100)));

		assertEquals(
			"Extinction Man Soul Log\n"
				+ "Target\tProgress\tStatus\n"
				+ "Hellhound\t1/100\t99 remaining\n"
				+ "Goblin\t100/100\tEXTINCT\n",
			result);
	}
}
