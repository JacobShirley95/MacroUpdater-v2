package org.auriferous.macrodeob.hooks;

import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class NodeIterableCollection extends Hook{

	@Override
	public boolean accept(TransformClassNode tcn) {
		int count = 0;
		for (String s : tcn.interfaces) {
			if (s.equals("java/lang/Iterable") || s.equals("java/util/Collection")) {
				count++;
			}
		}
		if (count == 2) {
			for (MethodNode mn : tcn.methods) {
				if (mn.name.equals("toArray")) {
					AbstractInsnNode in = new InsnSearcher(mn).searchSingle("invokevirtual");
					if (in != null) {
						MethodNode mn2 = getMethod((MethodInsnNode)in);
						FieldInsnNode fin = (FieldInsnNode) new InsnSearcher(mn2).searchSingle("getfield");
						HooksMap.CLIENT_HOOKS_MAP.addClassHook("IterableNodeList", tcn).addFieldHook("Current", fin);
						return true;
					}
				}
			}
		}
		return false;
	}

}
