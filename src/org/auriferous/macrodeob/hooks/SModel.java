package org.auriferous.macrodeob.hooks;

import java.util.List;

import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearchPlus;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class SModel extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {
		//"[S" == 8
		//[[F == 2
		//[F == 4
		if (countFieldsWithDescs(tcn, "[[F", "[F", "[S") == 14) {
			for (MethodNode mn : tcn.methods) {
				if (!isStatic(mn) && mn.desc.equals("(III)V")) {
					InsnSearchPlus finder = new InsnSearchPlus(mn);
					List<AbstractInsnNode[]> results = finder
							.search("monitorenter");
					if (results.size() > 0) {
						ClassHook ch = HooksMap.CLIENT_HOOKS_MAP.addClassHook("SModel", tcn);
						results = finder.search("getfield", results.get(0)[0]);

						ch.addFieldHook("VertexCount",
								(FieldInsnNode) results.get(0)[0]);
						ch.addFieldHook("XCoords",
								(FieldInsnNode) results.get(1)[0]);
						ch.addFieldHook("YCoords",
								(FieldInsnNode) results.get(2)[0]);
						ch.addFieldHook("ZCoords",
								(FieldInsnNode) results.get(3)[0]);

					}
				} else if (mn.desc.startsWith("(IIL") && mn.desc.endsWith(")Z")) {
					InsnSearchPlus finder = new InsnSearchPlus(mn);
					List<AbstractInsnNode[]> results = finder
							.search("invokestatic");
					if (results.size() > 0) {
						MethodInsnNode first = (MethodInsnNode) results.get(0)[0];
						if (first.name.equals("currentThread")) {
							results = finder
									.search("getfield");
							
							ClassHook ch = HooksMap.CLIENT_HOOKS_MAP.addClassHook("SModel", tcn);
							//ch.addFieldHook("ThreadResources", in)
						}
					}
				}
			}
			return true;
		}

		return false;
	}

}
