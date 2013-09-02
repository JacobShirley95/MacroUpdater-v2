package org.auriferous.macrodeob.hooks;

import java.util.Collections;
import java.util.List;

import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearchPlus;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class SModel extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {
		//"[S" == 8
		//[[F == 2
		//[F == 4
		if (countFieldsWithDescs(tcn, "[[F", "[F", "[S") >= 14) {
			for (MethodNode mn : tcn.methods) {
				if (!isStatic(mn) && mn.desc.startsWith("(IIL") && mn.desc.endsWith(")Z")) {
					InsnSearchPlus finder = new InsnSearchPlus(mn);
					List<AbstractInsnNode[]> results = finder
							.search("ldc fastore");
					for (AbstractInsnNode[] result : results) {
						if (((LdcInsnNode)result[0]).cst.toString().equals("-999999.0")) {
							System.out.println("DSFSDF");
							results = finder.searchBackward("getfield", result[0]);
							Collections.reverse(results);
							
							FieldInsnNode renderer = (FieldInsnNode)results.get(2)[0];
							
							ClassHook ch = HooksMap.CLIENT_HOOKS_MAP.addClassHook("SModel", tcn);
							ch.addFieldHook("ScaleY", results.get(1)[0]).addLink(renderer);
							ch.addFieldHook("RenderY", results.get(3)[0]).addLink(renderer);
							
							ch.addFieldHook("ScaleX", results.get(6)[0]).addLink(renderer);
							ch.addFieldHook("RenderX", results.get(8)[0]).addLink(renderer);
							
							results = finder.searchBackward("getfield", results.get(8)[0]);
							Collections.reverse(results);
							int c = 0;
							for (AbstractInsnNode[] result2 : results) {
								FieldInsnNode fin = (FieldInsnNode) result2[0];
								if (fin.desc.equals("[I")) {
									if (c == 0) {
										ch.addFieldHook("VerticesZ", fin);
										fin = (FieldInsnNode) finder.searchSingle("getfield", fin.getNext());
										ClassHook renderDat = HooksMap.CLIENT_HOOKS_MAP.addClassHook("RenderData", fin.owner);
										renderDat.addFieldHook("Data", fin);
									} else if (c == 1)
										ch.addFieldHook("VerticesY", fin);
									else {
										ch.addFieldHook("VerticesX", fin);
										ch.addFieldHook("VertexCount", finder.searchBackwardSingle("getfield", fin));
									}
									c++;
								}
								if (c == 3)
									break;
							}
							
							results = finder.search("getfield");
							ch.addFieldHook("RenderData", results.get(6)[0]).addLink(renderer);
							
							return true;
						}
					}
				}
			}
		}

		return false;
	}

}
