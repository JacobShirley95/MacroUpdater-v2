package org.auriferous.macrodeob.hooks;

import java.lang.reflect.Modifier;
import java.util.List;

import org.auriferous.macrodeob.Main;
import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.Type;

public class Entity extends Hook {

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
				tcn = (TransformClassNode) getSuper(tcn, 4);
				ClassHook animeHook = HooksMap.CLIENT_HOOKS_MAP.addClassHook(
						"Entity", tcn);
				List<MethodNode> mns = getMethodConstructors(tcn);
				if (mns != null) {
					for (MethodNode mn : mns) {
						InsnSearcher finder = new InsnSearcher(mn);
						List<AbstractInsnNode[]> results = finder
								.search("new dup invokespecial putfield");
						if (!results.isEmpty()) {

							for (AbstractInsnNode[] result : results) {
								tcn = Main.rsClassLoader
										.loadClass(((TypeInsnNode) result[0]).desc);
								for (MethodNode mn2 : tcn.methods) {
									if (containsLDC(mn2, " - ") != null) {
										FieldInsnNode fin = (FieldInsnNode) result[3];

										animeHook.addFieldHook("Viewport",
												fin);

										return true;
									}
								}
							}
						}
					}
				}
			}
		}

		return false;
	}

	private ClassNode getSuper(ClassNode cn, int count) {
		ClassNode cn2 = cn;
		for (int i = 0; i < count; i++)
			cn2 = Main.rsClassLoader.loadClass(cn2.superName);
		return cn2;
	}
}
