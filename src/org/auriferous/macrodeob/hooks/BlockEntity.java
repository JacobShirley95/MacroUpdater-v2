package org.auriferous.macrodeob.hooks;

import java.lang.reflect.Modifier;
import java.util.List;

import org.auriferous.macrodeob.Main;
import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class BlockEntity extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {

		int stringCount = 0;
		for (FieldNode fn : tcn.fields) {
			if (!Modifier.isStatic(fn.access)
					&& fn.desc.equals("Ljava/lang/String;")) {
				stringCount++;
			}
		}

		if (stringCount == 4) {
			for (MethodNode mn : tcn.methods) {
				if (!Modifier.isStatic(mn.access)
						&& mn.desc.endsWith("java/lang/String;")) {
					stringCount++;
				}
			}

			if (stringCount >= 5) {
				tcn = Main.rsClassLoader.loadClass(Main.rsClassLoader
						.loadClass(tcn.superName).superName);
				ClassHook blockEntityHook = HooksMap.CLIENT_HOOKS_MAP.addClassHook(
						"BlockEntity", tcn);
				for (MethodNode mn : tcn.methods) {
					if (!isStatic(mn) && !Modifier.isPublic(mn.access)
							&& mn.desc.startsWith("([")
							&& mn.desc.endsWith(")I")) {
						InsnSearcher finder = new InsnSearcher(mn);
						List<AbstractInsnNode[]> results = finder
								.search("getfield");
						if (!results.isEmpty()) {
							int i = 0;
							for (AbstractInsnNode[] result : results) {
								FieldInsnNode fin = (FieldInsnNode)result[0];
								if (fin.desc.equals("S") && fin.owner.equals(tcn.name)) {
									switch (i) {
									case 0: {blockEntityHook.addFieldHook("LocX1", result[0]);break;}
									case 1: {blockEntityHook.addFieldHook("LocX2", result[0]);break;}
									case 2: {blockEntityHook.addFieldHook("LocY1", result[0]);break;}
									case 3: {blockEntityHook.addFieldHook("LocY2", result[0]);break;}
									}
									i++;
									if (i == 4)
										break;
								}
							}
							for (AbstractInsnNode[] result : results) {
								FieldInsnNode fin = (FieldInsnNode)result[0];
								if (fin.desc.equals("B") && fin.owner.equals(tcn.name)) {
									FieldInsnNode plane = (FieldInsnNode)result[0];
									ClassHook movableHook = HooksMap.CLIENT_HOOKS_MAP.addClassHook(
											"MovableEntity", plane);
									movableHook.addFieldHook("Plane", plane);
									break;
								}
							}
							
							return true;
						}
					}
				}
			}
		}

		return false;
	}
}
