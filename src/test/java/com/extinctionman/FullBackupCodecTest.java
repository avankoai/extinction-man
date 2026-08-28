package com.extinctionman;

import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FullBackupCodecTest
{
	@Test
	public void roundTripsSoulAndMetaStorage()
	{
		String souls = "EM2\nR29ibGlu=100";
		String meta = MetaProgressCodec.encode(Collections.singleton("Goblin"), Collections.emptyList());
		FullBackupCodec.Decoded decoded = FullBackupCodec.decode(FullBackupCodec.encode(souls, meta));
		assertEquals(souls, decoded.getSoulStorage());
		assertEquals(meta, decoded.getMetaStorage());
	}
}
