package org.auriferous.macrodeob.hooks;

import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class PlayerArrayData extends Hook{

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (isStatic(mn) && mn.desc.startsWith("(Ljava/lang/String;") && containsGameString(mn, "Unable to find ")) {
				int count = 0;
				for (AbstractInsnNode in : mn.instructions.toArray()) {
					if (in.getOpcode() == Opcodes.GETSTATIC) {
						count++;
						if (count == 3) {
							FieldInsnNode fin = (FieldInsnNode)in;
							HooksMap.CLIENT_HOOKS_MAP.addClassHook("Client", "client").addStaticFieldHook("PlayerCount", fin);
						} else if (count == 4) {
							FieldInsnNode fin = (FieldInsnNode)in;
							HooksMap.CLIENT_HOOKS_MAP.addClassHook("Client", "client").addStaticFieldHook("PlayerIndices", fin);
							//break;
						} else if (count == 5) {
							FieldInsnNode fin = (FieldInsnNode)in;
							HooksMap.CLIENT_HOOKS_MAP.addClassHook("Client", "client").addStaticFieldHook("Players", fin);
							
							InsnSearcher finder = new InsnSearcher(mn);
							HooksMap.CLIENT_HOOKS_MAP.addClassHook("Player", getDesc(fin)).addFieldHook("Name", finder.searchSingle("getfield", fin));
							
							break;
						}
					}
				}
				break;
			}
		}
		return false;
	}
	
}
