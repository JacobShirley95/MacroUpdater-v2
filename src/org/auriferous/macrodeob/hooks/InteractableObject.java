package org.auriferous.macrodeob.hooks;

import java.util.List;

import org.auriferous.macrodeob.Main;
import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class InteractableObject extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (mn.instructions.size() == 0)
				break;
			
			InsnSearcher finder = new InsnSearcher(mn);
			List<AbstractInsnNode[]> results = finder
					.search("iload[0-9]+ ldc ior istore[0-9]+");
			for (AbstractInsnNode[] result : results) {
				if ((int) ((LdcInsnNode) result[1]).cst == 65536) {
					if (!isStatic(mn) && mn.name.equals("<init>")
							&& Type.getArgumentTypes(mn.desc).length >= 16) {
						System.out.println("DSFDSF");
						ClassHook ch = HooksMap.CLIENT_HOOKS_MAP.addClassHook(
								"InteractableObject", tcn);
						
						ch.addFieldHook("Model",
								finder.searchSingle("putfield", result[1]));
						
						FieldInsnNode f = (FieldInsnNode) finder.searchSingle(
								"putfield", 1);
						
						for (MethodNode mn2 : tcn.methods)
							if (!isStatic(mn2)) {
								InsnSearcher finder2 = new InsnSearcher(mn2);
								List<AbstractInsnNode[]> results2 = finder2
										.search("getfield");
								if (results2.size() > 1) {
									FieldInsnNode f2 = (FieldInsnNode) results2
											.get(1)[0];
									if (f.name.equals(f2.name)
											&& f.desc.equals(f2.desc)
											&& f.owner.equals(f2.owner)) {
										results2 = finder2
												.search("iconst_0 aaload");
										if (!results2.isEmpty()) {
											ch.addFieldHook("ID", f2);
										}
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
