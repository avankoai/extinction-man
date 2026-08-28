package com.extinctionman;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

final class CollectionLogPopupRenderer
{
	static final Color ORANGE = new Color(255, 152, 31);
	static final Color WHITE = new Color(238, 238, 238);
	static final Color SOUL_BLUE = new Color(170, 225, 255);
	static final Font HEADER_FONT = new Font(Font.SERIF, Font.BOLD, 20);
	static final Font MAIN_FONT = new Font(Font.SERIF, Font.BOLD, 18);
	static final Font DETAIL_FONT = new Font(Font.SERIF, Font.BOLD, 13);
	private static final Color STONE_DARK = new Color(43, 42, 35, 250);
	private static final Color STONE = new Color(100, 96, 79, 250);
	private static final Color STONE_LIGHT = new Color(153, 145, 116, 250);
	private static final Color PANEL = new Color(55, 47, 37, 250);
	private static final Color PANEL_EDGE = new Color(25, 24, 21, 250);

	private CollectionLogPopupRenderer() {}

	static void drawFrame(Graphics2D graphics, int x, int y, int width, int height, String header)
	{
		graphics.setColor(STONE_DARK);
		graphics.fillRect(x, y, width, height);
		graphics.setColor(STONE);
		graphics.fillRect(x + 3, y + 3, width - 6, height - 6);
		graphics.setColor(STONE_LIGHT);
		graphics.drawRect(x + 4, y + 4, width - 9, height - 9);
		graphics.setColor(PANEL_EDGE);
		graphics.fillRect(x + 11, y + 11, width - 22, height - 22);
		graphics.setColor(PANEL);
		graphics.fillRect(x + 15, y + 15, width - 30, height - 30);

		int dividerY = y + 45;
		graphics.setColor(PANEL_EDGE);
		graphics.fillRect(x + 12, dividerY, width - 24, 5);
		graphics.setColor(STONE_LIGHT.darker());
		graphics.drawLine(x + 15, dividerY - 1, x + width - 16, dividerY - 1);

		drawCorner(graphics, x + 4, y + 4, 1, 1);
		drawCorner(graphics, x + width - 5, y + 4, -1, 1);
		drawCorner(graphics, x + 4, y + height - 5, 1, -1);
		drawCorner(graphics, x + width - 5, y + height - 5, -1, -1);
		drawCentered(graphics, header, x, width, y + 35, HEADER_FONT, ORANGE);
	}

	static void drawCentered(Graphics2D graphics, String text, int x, int width, int baseline,
		Font font, Color color)
	{
		graphics.setFont(font);
		FontMetrics metrics = graphics.getFontMetrics();
		int textX = x + (width - metrics.stringWidth(text)) / 2;
		graphics.setColor(new Color(0, 0, 0, 210));
		graphics.drawString(text, textX + 2, baseline + 2);
		graphics.setColor(color);
		graphics.drawString(text, textX, baseline);
	}

	private static void drawCorner(Graphics2D graphics, int x, int y, int horizontal, int vertical)
	{
		graphics.setStroke(new BasicStroke(2));
		graphics.setColor(STONE_LIGHT);
		graphics.drawLine(x, y + 8 * vertical, x + 8 * horizontal, y);
		graphics.setColor(STONE_DARK);
		graphics.drawLine(x + 3 * horizontal, y + 10 * vertical, x + 10 * horizontal, y + 3 * vertical);
		graphics.setStroke(new BasicStroke(1));
	}
}
