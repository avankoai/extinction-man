package com.extinctionman;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

final class ExtinctGhostOverlay extends Overlay
{
	private static final Color GHOST_GLOW = new Color(175, 215, 245, 70);
	private static final Color GHOST_EDGE = new Color(215, 235, 250, 140);
	private final ExtinctGhostTracker tracker;
	private final ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	ExtinctGhostOverlay(ExtinctGhostTracker tracker,
		ModelOutlineRenderer modelOutlineRenderer)
	{
		this.tracker = tracker;
		this.modelOutlineRenderer = modelOutlineRenderer;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		for (NPC npc : tracker.active())
		{
			modelOutlineRenderer.drawOutline(npc, 4, GHOST_GLOW, 4);
			modelOutlineRenderer.drawOutline(npc, 1, GHOST_EDGE, 4);
		}
		return null;
	}
}
