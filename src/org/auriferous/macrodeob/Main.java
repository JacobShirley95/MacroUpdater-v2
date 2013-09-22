package org.auriferous.macrodeob;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.auriferous.macrodeob.hooks.*;
import org.auriferous.macrodeob.hooks.injector.Injector;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.RsClassLoader;

public class Main {
	private HashMap<Hook, Integer> hooks;
	public static RsClassLoader rsClassLoader;
	public static RsClassLoader dumpClassLoader;
	public static HashMap<String, String> clientMapping = new HashMap<String, String>();

	public Main() {
		System.out.println("loading classes...");
		loadClasses("../Deobfuscator2/runescape-deob3.jar", "../RsDecoder/runescape.jar");
		System.out.println("finding and injecting hooks...");
		injectHooks();
		System.out.println("writing classes...");
		writeClasses("runescape.jar");
	}
	
	private void injectHooks() {
		hooks = new LinkedHashMap<>();
		
		hooks.put(new Player(), 0);
		hooks.put(new NPC(), 0);
		hooks.put(new SoftwareRenderer(), 0);
		hooks.put(new SModel(), 0);
		hooks.put(new RSCharacter(), 0);
		hooks.put(new Entity(), 0);
		hooks.put(new MyPlayer(), 0);
		hooks.put(new GroundObjects(), 0);
		hooks.put(new Node(), 0);
		hooks.put(new NodeArrayList(), 0);
		hooks.put(new BlockEntity(), 0);
		hooks.put(new PlayerArrayData(), 0);
		hooks.put(new NPCData(), 0);
		hooks.put(new FriendIgnoreFields(), 0);
		hooks.put(new RSInterface(), 0);
		hooks.put(new RSInterfaceGroup(), 0);
		hooks.put(new CacheObjectLoader(), 0);
		hooks.put(new CacheObjectLoaderClass(), 0);
		hooks.put(new SoftReference(), 0);
		hooks.put(new Toolkit(), 0);
		hooks.put(new CurrentRenderer(), 0);
		hooks.put(new Viewport(), 0);
		hooks.put(new MapBase(), 0);
		hooks.put(new NodeIterableCollection(), 0);
		hooks.put(new VisibleInterfaces(), 0);
		hooks.put(new CameraFields(), 0);
		hooks.put(new MouseKeyboard(), 0);
		hooks.put(new OptionsMenu(), 0);
		hooks.put(new TileHeight(), 0);
		hooks.put(new RSCharacterFields(), 0);
		hooks.put(new WorldObjectsField(), 0);
		hooks.put(new WorldObjects(), 0);
		hooks.put(new DoorDecor(), 0);
		hooks.put(new InteractableObject(), 0);
		
		int MAX_RUN_LEVEL = 1;
		int count = hooks.size()-1;
		for (int i = 0; i < MAX_RUN_LEVEL; i++) {
			if (count == 0)
				break;
			for (TransformClassNode tcn : rsClassLoader.classMap.values()) {
				for (Entry<Hook, Integer> entry : hooks.entrySet()) {
					if (entry.getValue() != i)
						continue;

					Hook hook = entry.getKey();
					if (!hook.isActive() || !hook.dependenciesMet())
						continue;

					if (hook.accept(tcn)) {
						hook.setActive(false);
						count--;
					}
				}
			}
		}
		
		Injector injector = new Injector(dumpClassLoader, HooksMap.CLIENT_HOOKS_MAP, "org/macronite2/hooks/");
		injector.injectAll();
	}

	private void loadClasses(String jarFile, String original) {
		try {
			rsClassLoader = new RsClassLoader(jarFile);
			dumpClassLoader = new RsClassLoader(original);
			dumpClassLoader.loadAll();
			rsClassLoader.loadAll();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeClasses(String output) {
		try {
			dumpClassLoader.dumpJar(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Main();
	}
}
