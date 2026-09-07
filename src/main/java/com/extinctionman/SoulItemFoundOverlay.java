package com.extinctionman;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

final class SoulItemFoundOverlay extends Overlay
{
	private static final Color SOUL_GOLD = new Color(255, 178, 25);
	private final SoulItemFoundTracker tracker;

	@Inject
	SoulItemFoundOverlay(SoulItemFoundTracker tracker)
	{
		this.tracker = tracker;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		long now = System.currentTimeMillis();
		SoulItemFound found = tracker.active(now);
		if (found == null) return null;

		float unfold = found.unfold(now, SoulItemFoundTracker.DURATION_MILLIS);
		ThemedPopupRenderer.draw(graphics, unfold, now, "Unbound Soul Found",
			found.getItemName(), SOUL_GOLD, "",
			CollectionLogPopupRenderer.WHITE, true);
		return null;
	}
}
