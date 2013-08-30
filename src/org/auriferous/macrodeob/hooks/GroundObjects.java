package org.auriferous.macrodeob.hooks;

import java.util.List;

import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class GroundObjects extends Hook{

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (isStatic(mn) && mn.desc.startsWith("(L") && containsGameString(mn, "Face here")) {
				for (AbstractInsnNode in : mn.instructions.toArray()) {
					if (in.getOpcode() == Opcodes.INVOKEINTERFACE) {
						InsnSearcher finder = new InsnSearcher(mn);
						List<AbstractInsnNode[]> results = finder.search("instanceof", in);
						if (results.size() > 0) {
							results = finder.search("getstatic", results.get(3)[0]);
							FieldInsnNode groundObjs = (FieldInsnNode) results.get(0)[0];
							HooksMap.CLIENT_HOOKS_MAP.addClientHook("GroundObjects", groundObjs);
							
							results = finder.search("checkcast", results.get(0)[0]);
							
							TypeInsnNode cast = (TypeInsnNode)results.get(0)[0];
							TypeInsnNode cast2 = (TypeInsnNode)results.get(1)[0];
							
							results = finder.search("getfield", groundObjs);
							
							FieldInsnNode fin = (FieldInsnNode) results.get(2)[0];
							FieldInsnNode fin2 = (FieldInsnNode) results.get(3)[0];
							
							HooksMap.CLIENT_HOOKS_MAP.addClassHook("GroundDataNode", cast.desc).addFieldHook("Objects", fin);
							HooksMap.CLIENT_HOOKS_MAP.addClassHook("GroundObjectNode", cast2.desc).addFieldHook("ID", fin2);
							
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
}
