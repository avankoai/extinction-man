package com.extinctionman;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

final class SoulImageLoader
{
	private SoulImageLoader()
	{
	}

	static BufferedImage load(int width, int height)
	{
		try (InputStream stream = SoulImageLoader.class.getResourceAsStream("/com/extinctionman/soul.png"))
		{
			if (stream == null)
			{
				throw new IllegalStateException("Missing soul image");
			}
			BufferedImage source = ImageIO.read(stream);
			BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = scaled.createGraphics();
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics.drawImage(source, 0, 0, width, height, null);
			graphics.dispose();
			return scaled;
		}
		catch (IOException ex)
		{
			throw new IllegalStateException("Unable to load soul image", ex);
		}
	}
}
