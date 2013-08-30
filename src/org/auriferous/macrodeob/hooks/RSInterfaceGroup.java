package org.auriferous.macrodeob.hooks;

import java.util.List;

import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class RSInterfaceGroup extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (isStatic(mn) && mn.desc.startsWith("(II") && mn.desc.endsWith(")Z")) {
				if (mn.instructions.size() == 0)
					continue;
				
				InsnSearcher finder = new InsnSearcher(mn);
				List<AbstractInsnNode[]> results = finder.search(".ipush");
				int size = results.size();
				for (int i = 0; i < size; i++) {
					if (i + 4 < size) {
						if (((IntInsnNode) results.get(i)[0]).operand == 58
								&& ((IntInsnNode) results.get(i + 1)[0]).operand == 1007
								&& ((IntInsnNode) results.get(i + 2)[0]).operand == 25
								&& ((IntInsnNode) results.get(i + 3)[0]).operand == 57
								&& ((IntInsnNode) results.get(i + 4)[0]).operand == 30) {

							List<AbstractInsnNode[]> results2 = finder.search("getstatic");
							for (AbstractInsnNode[] insns : results2) {
								if (((FieldInsnNode)insns[0]).desc.startsWith("[L")) {
									ClassHook client = HooksMap.CLIENT_HOOKS_MAP.addClassHook("Client", "client");
									client.addStaticFieldHook("InterfaceGroups", (FieldInsnNode)insns[0]);
									
									results2 = finder.search("getfield", insns[0]);
									ClassHook interfaceGroup = HooksMap.CLIENT_HOOKS_MAP.addClassHook("RSInterfaceGroup", getDesc((FieldInsnNode)insns[0]));
									interfaceGroup.addFieldHook("Interfaces", results2.get(0)[0]);
									
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

}
