package com.zoomkeybinding;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ModifierlessKeybind;
import net.runelite.client.config.Range;
import java.awt.event.KeyEvent;

@ConfigGroup("ZoomKeybinding")
public interface ZoomKeybindingConfig extends Config
{
	@ConfigItem(
			position = 1,
			keyName = "zoomIncrement",
			name = "Zoom increment",
			description = "The value with which the zoom value is changed on key press."
	)
	@Range(
			min = 0,
			max = 800
	)
	default int zoomIncrement() {
		return 50;
	}

	@ConfigItem(
			position = 2,
			keyName = "zoomInKey",
			name = "Zoom in key",
			description = "The key to zoom in."
	)
	default ModifierlessKeybind zoomInKey()
	{
		return new ModifierlessKeybind(KeyEvent.VK_PAGE_UP, 0);
	}

	@ConfigItem(
			position = 3,
			keyName = "zoomOutKey",
			name = "Zoom out key",
			description = "The key to zoom out."
	)
	default ModifierlessKeybind zoomOutKey()
	{
		return new ModifierlessKeybind(KeyEvent.VK_PAGE_DOWN, 0);
	}

	@ConfigItem(
			position = 4,
			keyName = "smoothZoom",
			name = "Smooth zoom",
			description = "Enable smooth zooming while holding keys. When enabled, zoom increment is divided into smaller steps for smoother motion."
	)
	default boolean smoothZoom()
	{
		return false;
	}
}