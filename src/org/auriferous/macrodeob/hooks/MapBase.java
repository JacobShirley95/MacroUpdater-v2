package org.auriferous.macrodeob.hooks;

import java.util.List;

import org.auriferous.macrodeob.Main;
import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class MapBase extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			AbstractInsnNode ldc = containsLDC(mn, "getcamerapos");
			if (isStatic(mn) && mn.desc.startsWith("(Ljava/lang/String;")
					&& ldc != null) {
				InsnSearcher finder = new InsnSearcher(mn);
				List<AbstractInsnNode[]> results = finder.search("getstatic",
						ldc);
				if (results.size() > 0) {
					HooksMap.CLIENT_HOOKS_MAP.addClientHook("GameWorld",
							results.get(0)[0]);
					MethodNode mn2 = getMethod((MethodInsnNode) finder
							.searchSingle("invokevirtual", results.get(0)[0]));

					InsnSearcher finder2 = new InsnSearcher(mn2);
					ClassHook gameWorld = HooksMap.CLIENT_HOOKS_MAP.addClassHook(
							"GameWorld",
							getDesc((FieldInsnNode) results.get(0)[0]));
					
					FieldInsnNode fin = (FieldInsnNode) finder2.searchSingle("getfield");
					gameWorld.addFieldHook("MapBase", fin);
					ClassNode mapBaseClazz = Main.rsClassLoader.loadClass(getDesc(fin));
					
					for (MethodNode mn3 : mapBaseClazz.methods) {
						if (!isStatic(mn3) && mn3.desc.endsWith(")I")) {
							finder2 = new InsnSearcher(mn3);
							if (finder2.search("ishl").size() == 2) {
								ClassHook mapBase = HooksMap.CLIENT_HOOKS_MAP.addClassHook("MapBase", mapBaseClazz);
								List<AbstractInsnNode[]> fields = finder2.search("getfield");
								
								mapBase.addFieldHook("Plane", fields.get(0)[0]);
								mapBase.addFieldHook("X", fields.get(1)[0]);
								mapBase.addFieldHook("Y", fields.get(2)[0]);
								return true;
							}
						}
					}
					
					return true;
				}
			}
		}

		return false;
	}

}
