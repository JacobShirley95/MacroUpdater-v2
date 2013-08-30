package org.auriferous.macrodeob.hooks;

import java.util.List;

import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class MyPlayer extends Hook {
	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (mn.instructions.size() == 0)
				continue;
			if (isStatic(mn)) {
				InsnSearcher finder = new InsnSearcher(mn);
				List<AbstractInsnNode[]> results = finder
						.search("ldc");
				for (AbstractInsnNode[] result : results) {
					if (((LdcInsnNode) result[0]).cst.toString()
							.equals("Pos: ")) {
						ClassHook clientHook = HooksMap.CLIENT_HOOKS_MAP
								.addClassHook("Client", "client");
						clientHook.addStaticFieldHook("MyPlayer",
								(FieldInsnNode) finder.search("getstatic", result[0]).get(0)[0]);
						return true;
					}
				}
			}
		}
		return false;
	}
}
