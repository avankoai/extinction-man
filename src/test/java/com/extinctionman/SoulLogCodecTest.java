package com.extinctionman;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SoulLogCodecTest
{
	@Test
	public void storageRoundTripsUnicodeNamesAndProgress()
	{
		Map<String, SoulProgress> source = new HashMap<>();
		source.put("Goblin", new SoulProgress("Goblin", 14));
		source.put("TzHaar-Mej — test", new SoulProgress("TzHaar-Mej — test", 100));

		Map<String, Integer> restored = SoulLogCodec.decodeStorage(SoulLogCodec.encodeStorage(source));
		assertEquals(Integer.valueOf(14), restored.get("Goblin"));
		assertEquals(Integer.valueOf(100), restored.get("TzHaar-Mej — test"));
	}

	@Test
	public void backupRoundTripsAndIgnoresZeroProgress()
	{
		Map<String, SoulProgress> source = new HashMap<>();
		source.put("Rat", new SoulProgress("Rat", 0));
		source.put("Hellhound", new SoulProgress("Hellhound", 1));

		Map<String, Integer> restored = SoulLogCodec.decodeBackup(SoulLogCodec.encodeBackup(source));
		assertEquals(1, restored.size());
		assertEquals(Integer.valueOf(1), restored.get("Hellhound"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsUnrelatedClipboardText()
	{
		SoulLogCodec.decodeBackup("not a backup");
	}
}
