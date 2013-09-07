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

public class WorldObjects extends Hook{

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (!isStatic(mn) && mn.name.equals("<init>")) {
				InsnSearcher finder = new InsnSearcher(mn);
				List<AbstractInsnNode[]> results = finder.search("sipush anewarray putfield");
				for (AbstractInsnNode[] result : results) {
					if (((IntInsnNode)result[0]).operand > 10000) {
						ClassHook ch = HooksMap.CLIENT_HOOKS_MAP.addClassHook("WorldObjects", tcn);
						for (MethodNode mn2 : tcn.methods) {
							if (!isStatic(mn2) && !mn2.name.equals("<init>")) {
								finder = new InsnSearcher(mn2);
								results = finder.search("getfield iconst_3 aaload");
								ClassHook tile = null;
								if (!results.isEmpty()) {
									ch.addFieldHook("Tiles", results.get(0)[0]);
									
									results = finder.search("getfield dup getfield iconst_1 isub i2b");
									FieldInsnNode fin = (FieldInsnNode) results.get(1)[0];
									tile = HooksMap.CLIENT_HOOKS_MAP.addClassHook("Tile", fin.owner);
									tile.addFieldHook("WallDecor", fin);
									
									results = finder.search("getfield");
									fin = (FieldInsnNode) results.get(3)[0];
									FieldInsnNode fin2 = (FieldInsnNode) results.get(4)[0];
									FieldInsnNode fin3 = (FieldInsnNode) results.get(8)[0];
									
									tile.addFieldHook("Interactable", fin);
									ClassHook interactable = HooksMap.CLIENT_HOOKS_MAP.addClassHook("Interactable", getDesc(fin));
									interactable.addFieldHook("Object", fin2);
									interactable.addFieldHook("Next", fin3);
								}
							}
						}
						
						return true;
					}
				}
			}
		}
		return false;
	}
	
}
