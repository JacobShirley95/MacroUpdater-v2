package org.auriferous.macrodeob.hooks;

import java.util.List;

import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class EntityStack extends Hook{

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (isStatic(mn) && mn.instructions.size() > 0) {
				InsnSearcher finder = new InsnSearcher(mn);
				List<AbstractInsnNode[]> results = finder.search("getstatic aload([0-9])+ invokevirtual");
				if (!results.isEmpty() && ((FieldInsnNode)results.get(0)[0]).desc.equals("Ljava/util/Stack;")) {
					if (((MethodInsnNode)results.get(0)[2]).name.equals("push")) {
						HooksMap.CLIENT_HOOKS_MAP.addClientHook("EntityStack", results.get(0)[0]);
						
						FieldInsnNode fin = (FieldInsnNode) finder.searchSingle("putfield");
						ClassHook stackNode = HooksMap.CLIENT_HOOKS_MAP.addClassHook("StackNode", fin.owner);
						stackNode.addFieldHook("Entity", fin);
						
						return true;
					}
				}
			}
		}
		return false;
	}

}
