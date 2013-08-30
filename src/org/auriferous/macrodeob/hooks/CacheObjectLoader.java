package org.auriferous.macrodeob.hooks;

import java.util.List;

import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class CacheObjectLoader extends Hook{

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
							FieldInsnNode objectLoader = (FieldInsnNode) results.get(1)[0];
							HooksMap.CLIENT_HOOKS_MAP.addClientHook("CacheObjectLoader", objectLoader);

							ClassHook cacheLoader = HooksMap.CLIENT_HOOKS_MAP.addClassHook("CacheObjectLoader", getDesc(objectLoader));
							cacheLoader.addMethodHook("loadObject", (MethodInsnNode)finder.searchSingle("invokevirtual", objectLoader), 1);
							
							results = finder.search("getfield", objectLoader);
							
							FieldInsnNode isScenery = (FieldInsnNode) results.get(1)[0];
							FieldInsnNode textColour = (FieldInsnNode) results.get(2)[0];
							FieldInsnNode inBank = (FieldInsnNode) results.get(3)[0];
							
							ClassHook rsObject = HooksMap.CLIENT_HOOKS_MAP.addClassHook("RSObject", isScenery.owner);
							
							rsObject.addFieldHook("Scenery", isScenery);
							rsObject.addFieldHook("TextColour", textColour);
							rsObject.addFieldHook("InBank", inBank);
							
							results = finder.search("sipush istore[0-9]*", inBank);
							
							rsObject.addFieldHook("Actions", finder.searchBackwardSingle("getfield", results.get(0)[0]));
							rsObject.addFieldHook("Name", finder.searchSingle("getfield", results.get(0)[0]));
							
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
}
