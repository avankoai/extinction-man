package com.extinctionman;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

final class ForbiddenLootOverlay extends Overlay
{
	private static final Color FORBIDDEN_TEXT = new Color(135, 138, 145);
	private final Client client;
	private final ForbiddenLootTracker tracker;

	@Inject
	ForbiddenLootOverlay(Client client, ForbiddenLootTracker tracker)
	{
		this.client = client;
		this.tracker = tracker;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		for (ForbiddenLootMarker marker : tracker.active(client.getTickCount()))
		{
			LocalPoint localPoint = LocalPoint.fromWorld(client, marker.getWorldPoint());
			if (localPoint == null)
			{
				continue;
			}

			int offset = 25;
			for (String itemName : marker.getItemNames())
			{
				String label = lockedLabel(itemName);
				Point textLocation = Perspective.getCanvasTextLocation(
					client, graphics, localPoint, label, offset);
				if (textLocation != null)
				{
					OverlayUtil.renderTextLocation(graphics, textLocation, label, FORBIDDEN_TEXT);
				}
				offset += 14;
			}
		}
		return null;
	}

	static String lockedLabel(String itemName)
	{
		return itemName + " (Locked)";
	}
}
