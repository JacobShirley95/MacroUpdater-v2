package org.auriferous.macrodeob.hooks;

import java.util.List;

import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class SoftwareRenderer extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (containsLDC(mn, "Pure Java") != null) {
				ClassHook renderer = HooksMap.CLIENT_HOOKS_MAP
						.addClassHook("Renderer", tcn.superName);

				for (MethodNode mn2 : tcn.methods) {
					if (!isStatic(mn2) && mn2.desc.startsWith("(FFF[F")
							&& mn2.desc.endsWith(")V")) {
						InsnSearcher finder = new InsnSearcher(mn2);
						if (finder.search("fcmpl").isEmpty()) {
							renderer.addMethodHook("toScreen", tcn.superName, mn2, false, 4);
						}
					}
				}

				return true;
			}
		}
		return false;
	}

}
