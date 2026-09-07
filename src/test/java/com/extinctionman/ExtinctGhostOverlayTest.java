package com.extinctionman;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExtinctGhostOverlayTest
{
	@Test
	public void onlyExtinctExceptionsBecomeGhosts()
	{
		assertFalse(ExtinctionVisibility.shouldGhost(false, true, false));
		assertFalse(ExtinctionVisibility.shouldGhost(true, false, false));
		assertFalse(ExtinctionVisibility.shouldGhost(true, true, true));
		assertTrue(ExtinctionVisibility.shouldGhost(true, true, false));
	}

	@Test
	public void drawsPersistentGoldenExceptionOrb()
	{
		BufferedImage frame = new BufferedImage(30,30,BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = frame.createGraphics();
		try { SoulOrbRenderer.drawException(graphics, 15, 15, .25); }
		finally { graphics.dispose(); }
		int alpha = frame.getRGB(15,15) >>> 24;
		int red = frame.getRGB(15,15) >> 16 & 0xff;
		int blue = frame.getRGB(15,15) & 0xff;
		assertTrue(alpha > 0);
		assertTrue(red > blue);
		assertEquals(0, frame.getRGB(0,0));
	}

	@Test
	public void drawsPersistentBlueSlayerOrb()
	{
		BufferedImage frame = new BufferedImage(30,30,BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = frame.createGraphics();
		try { SoulOrbRenderer.drawSlayerException(graphics, 15, 15, .25); }
		finally { graphics.dispose(); }
		int red = frame.getRGB(15,15) >> 16 & 0xff;
		int blue = frame.getRGB(15,15) & 0xff;
		assertTrue(blue > red);
		assertEquals(0, frame.getRGB(0,0));
	}
}
