package com.extinctionman;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

final class ExtinctionCelebrationOverlay extends Overlay
{
	private final ExtinctionManConfig config;
	private final ExtinctionCelebrationTracker tracker;

	@Inject
	ExtinctionCelebrationOverlay(ExtinctionManConfig config, ExtinctionCelebrationTracker tracker)
	{
		this.config = config;
		this.tracker = tracker;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showExtinctionPopup()) return null;
		long now = System.currentTimeMillis();
		ExtinctionCelebration celebration = tracker.active(now);
		if (celebration == null) return null;
		ThemedPopupRenderer.draw(graphics,
			celebration.unfold(now, ExtinctionCelebrationTracker.DURATION_MILLIS), now,
			"Exterminated", celebration.getNpcName(), CollectionLogPopupRenderer.WHITE,
			celebration.getCompletionText(), CollectionLogPopupRenderer.SOUL_BLUE, false);
		return null;
	}
}
