package org.auriferous.macrodeob.hooks;

import java.util.List;

import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class VisibleInterfaces extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (isStatic(mn) && mn.desc.endsWith(")V") && mn.instructions.size() > 0 && mn.instructions.size() < 25) {
				InsnSearcher finder = new InsnSearcher(mn);
				List<AbstractInsnNode[]> results = finder.search("invokevirtual newarray putstatic return");

				for (int i = 0; i < results.size(); i++) {
					AbstractInsnNode[] result = results.get(i);
					IntInsnNode type = (IntInsnNode) result[1];
					if (type.operand == 4) {
						HooksMap.CLIENT_HOOKS_MAP.addClientHook(
								"VisibleInterfaces", result[2]);
						return true;
					}
				}
			}
		}
		return false;
	}

}
