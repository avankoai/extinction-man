package com.extinctionman;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

final class ExtinctGhostOverlay extends Overlay
{
	private static final long ANIMATION_MILLIS = 1800L;
	private final Client client;
	private final ExtinctGhostTracker tracker;

	@Inject
	ExtinctGhostOverlay(Client client, ExtinctGhostTracker tracker)
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
		for (NPC npc : tracker.active())
		{
			LocalPoint localPoint = npc.getLocalLocation();
			if (localPoint == null)
			{
				continue;
			}

			int height = Math.max(45, npc.getLogicalHeight() / 2);
			Point center = Perspective.localToCanvas(client, localPoint, npc.getWorldLocation().getPlane(), height);
			if (center == null)
			{
				continue;
			}

			double phase = ((now + npc.getIndex() * 137L) % ANIMATION_MILLIS)
				/ (double) ANIMATION_MILLIS;
			SoulOrbRenderer.drawException(graphics, center.getX(), center.getY(), phase,
				tracker.getType(npc));
		}
		return null;
	}
}
