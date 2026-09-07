package com.extinctionman;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

/** Small soul wisps with a pulsing halo and orbiting sparks. */
final class SoulOrbRenderer
{
	private SoulOrbRenderer() {}

	static void draw(Graphics2D graphics, int x, int y, double progress)
	{
		draw(graphics, x, y, progress, false);
	}

	static void draw(Graphics2D graphics, int x, int y, double progress, boolean extinction)
	{
		draw(graphics, x, y, progress, extinction, false);
	}

	static void draw(Graphics2D graphics, int x, int y, double progress,
		boolean extinction, boolean golden)
	{
		Graphics2D g = (Graphics2D) graphics.create();
		try
		{
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			float alpha = (float) Math.max(0, Math.min(1, Math.min(progress * 10, (1 - progress) * 2)));
			g.setComposite(AlphaComposite.SrcOver.derive(alpha));
			float cx = x + (float) Math.sin(progress * Math.PI * 3) * 4;
			float cy = y;
			float radius = 11 + (float) Math.sin(progress * Math.PI * 8) * 1.5f;
			Color[] colors = extinction
				? new Color[]{new Color(255, 230, 225), new Color(255, 100, 95, 240),
					new Color(240, 30, 40, 130), new Color(180, 10, 25, 0)}
				: golden
					? new Color[]{new Color(255, 255, 235), new Color(255, 238, 135, 245),
						new Color(255, 178, 25, 165), new Color(210, 105, 0, 0)}
					: new Color[]{new Color(240, 255, 255), new Color(145, 235, 255, 240),
						new Color(35, 150, 255, 130), new Color(20, 90, 255, 0)};
			g.setPaint(new RadialGradientPaint(cx, cy, radius,
				new float[]{0, .22f, .5f, 1}, colors));
			g.fill(new Ellipse2D.Float(cx - radius, cy - radius, radius * 2, radius * 2));
			for (int i = 0; i < 6; i++)
			{
				double phase = progress * Math.PI * 5 + i * Math.PI / 3;
				int sx = (int) (cx + Math.cos(phase) * (8 + i));
				int sy = (int) (cy + Math.sin(phase) * 5 + i * 3);
				int opacity = (int) (80 + 175 * (.5 + .5 * Math.sin(phase * 2)));
				g.setColor(extinction ? new Color(255, 135, 125, opacity)
					: golden ? new Color(255, 226, 105, opacity)
					: new Color(175, 235, 255, opacity));
				g.drawLine(sx - 1, sy, sx + 1, sy);
				g.drawLine(sx, sy - 1, sx, sy + 1);
			}
		}
		finally { g.dispose(); }
	}

	static void drawException(Graphics2D graphics, int x, int y, double phase)
	{
		drawException(graphics, x, y, phase, SoulExceptionType.UNBOUND);
	}

	static void drawSlayerException(Graphics2D graphics, int x, int y, double phase)
	{
		drawException(graphics, x, y, phase, SoulExceptionType.SLAYER);
	}

	static void drawException(Graphics2D graphics, int x, int y, double phase,
		SoulExceptionType type)
	{
		Graphics2D g = (Graphics2D) graphics.create();
		try
		{
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setComposite(AlphaComposite.SrcOver.derive(0.9f));
			float pulse = (float) Math.sin(phase * Math.PI * 2 * 3);
			float radius = 8.5f + pulse;
			g.setPaint(new RadialGradientPaint(x, y, radius,
				new float[]{0, .20f, .52f, 1},
				exceptionColors(type)));
			g.fill(new Ellipse2D.Float(x - radius, y - radius, radius * 2, radius * 2));

			for (int i = 0; i < 4; i++)
			{
				double sparkPhase = phase * Math.PI * 2 * 2 + i * Math.PI / 2;
				int sx = (int) Math.round(x + Math.cos(sparkPhase) * (9 + i));
				int sy = (int) Math.round(y + Math.sin(sparkPhase) * (4 + i));
				int opacity = (int) (130 + 110 * (.5 + .5 * Math.sin(sparkPhase * 2)));
				g.setColor(exceptionSpark(type, opacity));
				g.drawLine(sx - 1, sy, sx + 1, sy);
				g.drawLine(sx, sy - 1, sx, sy + 1);
			}
		}
		finally { g.dispose(); }
	}

	private static Color[] exceptionColors(SoulExceptionType type)
	{
		if (type == SoulExceptionType.UNBOUND)
			return new Color[]{new Color(255, 255, 235), new Color(255, 238, 135, 245),
				new Color(255, 178, 25, 165), new Color(210, 105, 0, 0)};
		if (type == SoulExceptionType.STRONG)
			return new Color[]{new Color(190, 190, 200), new Color(55, 55, 65, 245),
				new Color(5, 5, 10, 210), new Color(0, 0, 0, 0)};
		if (type == SoulExceptionType.WEAK)
			return new Color[]{new Color(255, 255, 255), new Color(245, 245, 250, 245),
				new Color(190, 195, 205, 150), new Color(150, 155, 165, 0)};
		return new Color[]{new Color(240, 255, 255), new Color(145, 235, 255, 245),
			new Color(35, 150, 255, 165), new Color(20, 90, 255, 0)};
	}

	private static Color exceptionSpark(SoulExceptionType type, int opacity)
	{
		if (type == SoulExceptionType.UNBOUND) return new Color(255, 226, 105, opacity);
		if (type == SoulExceptionType.STRONG) return new Color(30, 30, 38, opacity);
		if (type == SoulExceptionType.WEAK) return new Color(250, 250, 255, opacity);
		return new Color(175, 235, 255, opacity);
	}
}
