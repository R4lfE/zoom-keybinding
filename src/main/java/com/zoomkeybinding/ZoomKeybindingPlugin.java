package com.zoomkeybinding;

import com.google.inject.Provides;
import java.awt.event.KeyEvent;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.ScriptID;
import net.runelite.api.VarClientInt;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
		name = "Zoom keybinding"
)
public class ZoomKeybindingPlugin extends Plugin implements KeyListener
{
	private static final int ZOOM_UPDATE_INTERVAL_MS = 16; // ~60 FPS
	private static final int SMOOTH_ZOOM_DIVISOR = 8; // Divides config increment for smooth updates

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ZoomKeybindingConfig config;

	@Inject
	private KeyManager keyManager;

	@Inject
	private ScheduledExecutorService scheduledExecutorService;

	private ScheduledFuture<?> zoomTask;
	private boolean zoomingIn = false;
	private boolean zoomingOut = false;

	@Provides
	ZoomKeybindingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ZoomKeybindingConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		keyManager.registerKeyListener(this);
	}

	@Override
	protected void shutDown() throws Exception
	{
		keyManager.unregisterKeyListener(this);
		stopSmoothZoom();
	}

	@Override
	public void keyPressed(KeyEvent keyEvent)
	{
		if (config.smoothZoom())
		{
			handleSmoothZoomKeyPressed(keyEvent);
		}
		else
		{
			handleRegularZoomKeyPressed(keyEvent);
		}
	}

	@Override
	public void keyReleased(KeyEvent keyEvent)
	{
		if (config.smoothZoom())
		{
			handleSmoothZoomKeyReleased(keyEvent);
		}
		// Regular zoom doesn't need keyReleased handling
	}

	@Override
	public void keyTyped(KeyEvent keyEvent)
	{
		// Not used
	}

	private void handleRegularZoomKeyPressed(KeyEvent keyEvent)
	{
		int zoomIncrement = 0;
		if (keyEvent.getKeyCode() == config.zoomInKey().getKeyCode())
		{
			zoomIncrement = config.zoomIncrement();
		}
		else if (keyEvent.getKeyCode() == config.zoomOutKey().getKeyCode())
		{
			zoomIncrement = -config.zoomIncrement();
		}

		if (zoomIncrement != 0)
		{
			final int currentZoomValue = client.getVarcIntValue(VarClientInt.CAMERA_ZOOM_FIXED_VIEWPORT);
			final int newZoomValue = currentZoomValue + zoomIncrement;
			clientThread.invokeLater(() ->
					client.runScript(ScriptID.CAMERA_DO_ZOOM, newZoomValue, newZoomValue)
			);
		}
	}

	private void handleSmoothZoomKeyPressed(KeyEvent keyEvent)
	{
		int keyCode = keyEvent.getKeyCode();

		if (keyCode == config.zoomInKey().getKeyCode() && !zoomingIn)
		{
			zoomingIn = true;
			startSmoothZoom();
		}
		else if (keyCode == config.zoomOutKey().getKeyCode() && !zoomingOut)
		{
			zoomingOut = true;
			startSmoothZoom();
		}
	}

	private void handleSmoothZoomKeyReleased(KeyEvent keyEvent)
	{
		if (keyEvent.getKeyCode() == config.zoomInKey().getKeyCode())
		{
			zoomingIn = false;
			if (!zoomingOut)
			{
				stopSmoothZoom();
			}
		}
		else if (keyEvent.getKeyCode() == config.zoomOutKey().getKeyCode())
		{
			zoomingOut = false;
			if (!zoomingIn)
			{
				stopSmoothZoom();
			}
		}
	}

	private void startSmoothZoom()
	{
		if (zoomTask != null && !zoomTask.isDone())
		{
			return; // Already zooming
		}

		zoomTask = scheduledExecutorService.scheduleAtFixedRate(() ->
		{
			if (!zoomingIn && !zoomingOut)
			{
				return;
			}

			int zoomIncrementPerUpdate = Math.max(1, config.zoomIncrement() / SMOOTH_ZOOM_DIVISOR);
			int totalZoomIncrement = 0;

			if (zoomingIn)
			{
				totalZoomIncrement += zoomIncrementPerUpdate;
			}
			if (zoomingOut)
			{
				totalZoomIncrement -= zoomIncrementPerUpdate;
			}

			final int currentZoomValue = client.getVarcIntValue(VarClientInt.CAMERA_ZOOM_FIXED_VIEWPORT);
			final int newZoomValue = currentZoomValue + totalZoomIncrement;

			clientThread.invokeLater(() ->
					client.runScript(ScriptID.CAMERA_DO_ZOOM, newZoomValue, newZoomValue)
			);

		}, 0, ZOOM_UPDATE_INTERVAL_MS, TimeUnit.MILLISECONDS);
	}

	private void stopSmoothZoom()
	{
		if (zoomTask != null && !zoomTask.isDone())
		{
			zoomTask.cancel(false);
		}
		zoomTask = null;
	}
}