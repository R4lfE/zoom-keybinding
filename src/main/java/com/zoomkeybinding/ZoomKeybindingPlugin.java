package com.zoomkeybinding;
import com.google.inject.Provides;

import java.awt.event.KeyEvent;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.ScriptID;
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
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ZoomKeybindingConfig config;
	
	@Inject
	private KeyManager keyManager;

	@Provides
	ZoomKeybindingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ZoomKeybindingConfig.class);
	}

	boolean zoomInDown = false;
	boolean zoomOutDown = false;
	int currentZoomValue;

	@Override
	protected void startUp() throws Exception
	{
		keyManager.registerKeyListener(this);
		currentZoomValue = client.get3dZoom();
	}

	@Override
	protected void shutDown() throws Exception
	{
		keyManager.unregisterKeyListener(this);
	}

	private void updateZoomValue(int increment) {
		currentZoomValue = Math.max(100, Math.min(currentZoomValue + increment, 900));
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == config.zoomInKey().getKeyCode()) {
			zoomInDown = true;
		} else if (e.getKeyCode() == config.zoomOutKey().getKeyCode()) {
			zoomInDown = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == config.zoomInKey().getKeyCode()) {
			zoomInDown = false;
			updateZoomValue(config.zoomIncrement());
		} else if (e.getKeyCode() == config.zoomOutKey().getKeyCode()) {
			zoomOutDown = false;
			updateZoomValue(-config.zoomIncrement());
		}
		final int zoomValue = currentZoomValue;
		clientThread.invokeLater(() -> client.runScript(ScriptID.CAMERA_DO_ZOOM, zoomValue, zoomValue));
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}
