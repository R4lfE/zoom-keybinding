package com.zoomkeybinding;
import com.google.inject.Provides;

import java.awt.event.KeyEvent;
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

	@Override
	protected void startUp() throws Exception
	{
		keyManager.registerKeyListener(this);
	}

	@Override
	protected void shutDown() throws Exception
	{
		keyManager.unregisterKeyListener(this);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int increment = 0;
		if (e.getKeyCode() == config.zoomInKey().getKeyCode()) {
			increment = config.zoomIncrement();
		} else if (e.getKeyCode() == config.zoomOutKey().getKeyCode()) {
			increment = -config.zoomIncrement();
		}

		final int zoomValue = client.getVarcIntValue(VarClientInt.CAMERA_ZOOM_FIXED_VIEWPORT) + increment;
		clientThread.invokeLater(() -> client.runScript(ScriptID.CAMERA_DO_ZOOM, zoomValue, zoomValue));
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}
