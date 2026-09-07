package com.extinctionman;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;

final class ThemedPopupRenderer
{
	private static final int WIDTH = 270;
	private static final int HEIGHT = 124;
	private static final Font HEADER_FONT = loadFont("runescape-bold-12.otf", 17.0f,
		CollectionLogPopupRenderer.HEADER_FONT);
	private static final Font MAIN_FONT = loadFont("runescape-plain-12.otf", 16.0f,
		CollectionLogPopupRenderer.MAIN_FONT);
	private static final BufferedImage BACKGROUND = loadBackground();

	private ThemedPopupRenderer() {}

	static void draw(Graphics2D graphics, float unfold, long now, String title,
		String mainText, Color mainColor, String detailText, Color detailColor,
		boolean drawGoldenOrb)
	{
		Rectangle clip = graphics.getClipBounds();
		int width = Math.min(WIDTH, Math.max(80, clip.width - 20));
		int x = clip.x + (clip.width - width) / 2;
		int y = clip.y + 40;
		int animatedWidth = Math.max(2, Math.round(width * unfold));
		int animatedX = x + (width - animatedWidth) / 2;
		Graphics2D popup = (Graphics2D) graphics.create();
		try
		{
			popup.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				Math.min(1.0f, unfold * 1.5f)));
			popup.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
			if (BACKGROUND != null)
			{
				popup.drawImage(BACKGROUND, animatedX, y, animatedWidth, HEIGHT, null);
			}
			else
			{
				CollectionLogPopupRenderer.drawFrame(popup, animatedX, y,
					animatedWidth, HEIGHT, "");
			}
			if (animatedWidth > 18)
			{
				popup.setColor(new Color(0, 0, 0, 235));
				popup.fillRect(animatedX + 9, y + 37, animatedWidth - 18, 1);
			}
			if (unfold <= 0.68f) return;
			float contentAlpha = Math.min(1.0f, (unfold - 0.68f) / 0.32f);
			popup.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, contentAlpha));
			drawCrispCentered(popup, title, x, width, y + 28, HEADER_FONT,
				CollectionLogPopupRenderer.ORANGE, true);
			drawCrispCentered(popup, mainText, x, width, y + 65, MAIN_FONT, mainColor, true);
			boolean hasDetail = detailText != null && !detailText.trim().isEmpty();
			if (hasDetail)
			{
				drawCrispCentered(popup, detailText, x, width, y + 91, MAIN_FONT, detailColor, true);
			}
			if (drawGoldenOrb)
			{
				double phase = (now % 1800L) / 1800.0;
				SoulOrbRenderer.drawException(popup, x + width / 2,
					y + (hasDetail ? 108 : 98), phase);
			}
		}
		finally { popup.dispose(); }
	}

	private static void drawCrispCentered(Graphics2D graphics, String text, int x, int width,
		int baseline, Font requestedFont, Color color, boolean shadow)
	{
		Graphics2D textGraphics = (Graphics2D) graphics.create();
		try
		{
			textGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			textGraphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
			Font font = fitFont(textGraphics, requestedFont, text, width - 20);
			textGraphics.setFont(font);
			FontMetrics metrics = textGraphics.getFontMetrics(font);
			int textX = x + (width - metrics.stringWidth(text)) / 2;
			if (shadow)
			{
				textGraphics.setColor(Color.BLACK);
				textGraphics.drawString(text, textX + 1, baseline + 1);
			}
			textGraphics.setColor(color);
			textGraphics.drawString(text, textX, baseline);
		}
		finally { textGraphics.dispose(); }
	}

	private static Font fitFont(Graphics2D graphics, Font requested, String text, int maxWidth)
	{
		Font font = requested;
		while (font.getSize2D() > 10.0f
			&& graphics.getFontMetrics(font).stringWidth(text) > maxWidth)
		{
			font = requested.deriveFont(font.getSize2D() - 1.0f);
		}
		return font;
	}

	private static BufferedImage loadBackground()
	{
		try (InputStream stream = ThemedPopupRenderer.class.getResourceAsStream("popup-border.jpeg"))
		{
			return stream == null ? null : ImageIO.read(stream);
		}
		catch (Exception ignored) { return null; }
	}

	private static Font loadFont(String resource, float size, Font fallback)
	{
		try (InputStream stream = ThemedPopupRenderer.class.getResourceAsStream(resource))
		{
			return stream == null ? fallback.deriveFont(size)
				: Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(size);
		}
		catch (Exception ignored) { return fallback.deriveFont(size); }
	}
}
