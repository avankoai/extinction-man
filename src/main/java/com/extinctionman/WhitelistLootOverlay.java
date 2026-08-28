package com.extinctionman;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

final class WhitelistLootOverlay extends Overlay
{
	private static final Color BEAM = new Color(115, 225, 255);
	private final Client client;
	private final WhitelistLootTracker tracker;

	@Inject
	WhitelistLootOverlay(Client client, WhitelistLootTracker tracker)
	{
		this.client = client;
		this.tracker = tracker;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		long now = System.currentTimeMillis();
		float pulse = (float) ((Math.sin(now / 210.0) + 1.0) * 0.5);
		Paint oldPaint = graphics.getPaint();
		Stroke oldStroke = graphics.getStroke();
		Composite oldComposite = graphics.getComposite();
		Object oldAntialiasing = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		for (WhitelistLootMarker marker : tracker.active(client.getTickCount()))
		{
			LocalPoint localPoint = LocalPoint.fromWorld(client, marker.getWorldPoint());
			if (localPoint == null) continue;
			Polygon tile = Perspective.getCanvasTilePoly(client, localPoint);
			Point base = Perspective.localToCanvas(client, localPoint,
				client.getTopLevelWorldView().getPlane());
			if (tile == null || base == null) continue;

			int x = base.getX();
			int bottom = base.getY();
			int height = 145 + Math.round(8 * pulse);
			int top = bottom - height;

			graphics.setColor(new Color(75, 205, 255, 24 + Math.round(22 * pulse)));
			graphics.fillPolygon(tile);
			graphics.setColor(new Color(125, 235, 255, 55 + Math.round(45 * pulse)));
			graphics.setStroke(new BasicStroke(4.5f + pulse * 2f,
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			graphics.drawPolygon(tile);

			graphics.setPaint(new RadialGradientPaint(x, bottom - 3, 34 + 7 * pulse,
				new float[]{0f, .38f, 1f},
				new Color[]{new Color(210, 250, 255, 165),
					new Color(80, 215, 255, 75), new Color(70, 190, 255, 0)}));
			graphics.fillOval(x - 42, bottom - 45, 84, 84);

			graphics.setPaint(new GradientPaint(x, top,
				new Color(120, 230, 255, 0), x, bottom,
				new Color(135, 235, 255, 80 + Math.round(45 * pulse))));
			graphics.fillRoundRect(x - 17, top, 34, height, 24, 24);
			graphics.setPaint(new GradientPaint(x, top,
				new Color(225, 255, 255, 8), x, bottom,
				new Color(225, 255, 255, 175 + Math.round(55 * pulse))));
			graphics.fillRoundRect(x - 7, top + 4, 14, height - 4, 12, 12);

			int moteY = bottom - 28 - (int) ((now / 18 + marker.getItemId()) % 105);
			graphics.setColor(new Color(225, 255, 255, 130 + Math.round(90 * pulse)));
			graphics.fillOval(x - 14, moteY, 5, 5);
			graphics.fillOval(x + 10, moteY + 31, 4, 4);
			Point text = Perspective.getCanvasTextLocation(client, graphics, localPoint,
				"Soul item: " + marker.getItemName(), height + 10);
			if (text != null)
			{
				OverlayUtil.renderTextLocation(graphics, text,
					"Soul item: " + marker.getItemName(), BEAM);
			}
		}
		graphics.setPaint(oldPaint);
		graphics.setStroke(oldStroke);
		graphics.setComposite(oldComposite);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasing);
		return null;
	}
}
