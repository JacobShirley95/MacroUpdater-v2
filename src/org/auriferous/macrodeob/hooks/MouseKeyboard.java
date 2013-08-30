package org.auriferous.macrodeob.hooks;

import java.util.List;

import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class MouseKeyboard extends Hook{

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (isStatic(mn) && mn.desc.endsWith(")V") && mn.instructions.size() > 0) {
				InsnSearcher finder = new InsnSearcher(mn);
				List<AbstractInsnNode[]> results = finder.search("invokevirtual");
				for (AbstractInsnNode[] result : results) {
					if (((MethodInsnNode)result[0]).name.equals("setBackground")) {
						results = finder.search("putstatic", result[0]);
						FieldInsnNode mouseHandler = (FieldInsnNode)results.get(2)[0];
						
						HooksMap.CLIENT_HOOKS_MAP.addClassHook("MouseHandler", getDesc(mouseHandler));
						HooksMap.CLIENT_HOOKS_MAP.addClientHook("MouseHandler", mouseHandler);
						
						FieldInsnNode keyHandler = (FieldInsnNode)results.get(1)[0];
						
						HooksMap.CLIENT_HOOKS_MAP.addClassHook("KeyboardHandler", getDesc(keyHandler));
						HooksMap.CLIENT_HOOKS_MAP.addClientHook("KeyboardHandler", keyHandler);
						
						return true;
					}
				}
			}
		}
		return false;
	}

}
