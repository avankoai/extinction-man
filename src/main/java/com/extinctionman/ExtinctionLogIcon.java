package com.extinctionman;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

final class ExtinctionLogIcon
{
	private ExtinctionLogIcon()
	{
	}

	static BufferedImage create()
	{
		try (InputStream stream = ExtinctionLogIcon.class.getResourceAsStream("extinction-sigil.png"))
		{
			if (stream == null)
			{
				throw new IllegalStateException("Missing Extinction Sigil resource");
			}
			return ImageIO.read(stream);
		}
		catch (IOException ex)
		{
			throw new IllegalStateException("Unable to load Extinction Sigil", ex);
		}
	}
}
