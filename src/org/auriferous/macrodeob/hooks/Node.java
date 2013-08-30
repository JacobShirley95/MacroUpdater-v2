package org.auriferous.macrodeob.hooks;

import java.util.List;

import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class Node extends Hook{

	@Override
	public boolean accept(TransformClassNode tcn) {
		if (countFieldsWithDescs(tcn, "L"+tcn.name+";", "J") == 3 && countClassMethods(tcn) == 3) {
			for (MethodNode mn : tcn.methods) {
				if (!isStatic(mn) && mn.desc.endsWith(")V")) {
					ClassHook node = HooksMap.CLIENT_HOOKS_MAP.addClassHook("Node", tcn);
					InsnSearcher finder = new InsnSearcher(mn);
					List<AbstractInsnNode[]> results = finder.search("getfield");
					if (results.size() > 4) {
						node.addFieldHook("Next", (FieldInsnNode)results.get(1)[0]);
						node.addFieldHook("Base", (FieldInsnNode)results.get(2)[0]);
						return true;
					}
				}
			}
		}
		return false;
	}
	
}
