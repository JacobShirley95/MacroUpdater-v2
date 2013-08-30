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

public class NPCData extends Hook{

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (isStatic(mn) && mn.desc.startsWith("(L") && containsGameString(mn, "Face here")) {
				for (AbstractInsnNode in : mn.instructions.toArray()) {
					if (in.getOpcode() == Opcodes.INVOKEINTERFACE) {
						InsnSearcher finder = new InsnSearcher(mn);
						List<AbstractInsnNode[]> results = finder.search("instanceof", in);
						if (results.size() > 0) {
							results = finder.search("iconst_0 istore[0-9]* iload[0-9]*", results.get(1)[0]);
							results = finder.search("getstatic", results.get(0)[0]);
							HooksMap.CLIENT_HOOKS_MAP.addClientHook("AvailableNPCS", results.get(0)[0]);
							HooksMap.CLIENT_HOOKS_MAP.addClientHook("NPCIndices", results.get(2)[0]);
							HooksMap.CLIENT_HOOKS_MAP.addClientHook("NPCS", results.get(1)[0]);
							
							FieldInsnNode npcNodeObject = (FieldInsnNode) finder.searchSingle("getfield", results.get(1)[0]);
							ClassHook npcNode = HooksMap.CLIENT_HOOKS_MAP.addClassHook("NPCNode", npcNodeObject);
							npcNode.addFieldHook("Object", npcNodeObject);
							
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
}
