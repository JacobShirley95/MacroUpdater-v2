package org.auriferous.macrodeob.hooks;

import java.lang.reflect.Modifier;
import java.util.List;

import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class NodeArrayList extends Hook{

	@Override
	public boolean accept(TransformClassNode tcn) {
		if (getMethodsWithName(tcn, "iterator") != null && countFieldsWithDescs(tcn, "J") == 1 && countFieldsWithDescs(tcn, "I") == 2) {
			FieldNode nodeArray = null;
			int count = 0;
			for (FieldNode fn : tcn.fields) {
				if (!Modifier.isStatic(fn.access) && fn.desc.startsWith("L")) {
					for (FieldNode fn2 : tcn.fields) {
						if (!Modifier.isStatic(fn2.access)) {
							if (fn2.desc.equals("["+fn.desc) && nodeArray == null) {
								nodeArray = fn2;
								count++;
							} else if (fn2.desc.equals(fn.desc)) {
								count++;
							}
						}
					}
					break;
				}	
			}
			if (nodeArray != null && count == 3) {
				MethodNode mn = getMethodConstructor(tcn);
				if (mn != null) {
					ClassHook nodeArrayList = HooksMap.CLIENT_HOOKS_MAP.addClassHook("NodeArrayList", tcn);
					nodeArrayList.addFieldHook("NodeArray", nodeArray, null);
					
					for (MethodNode mn2 : tcn.methods) {
						if (!isStatic(mn2)) {
							for (AbstractInsnNode in : mn2.instructions.toArray()) {
								if (in.getOpcode() == Opcodes.GETFIELD) {
									FieldInsnNode fin = (FieldInsnNode)in;
									
									if (!fin.owner.equals(tcn.name) && fin.desc.equals("J")) {
										ClassHook node = HooksMap.CLIENT_HOOKS_MAP.addClassHook("Node", fin.owner);
										node.addFieldHook("ID", fin);
										return true;
									}
								}
							}
						}
					}
					
					return true;
				}
			}
		}
		return false;
	}
	
}
