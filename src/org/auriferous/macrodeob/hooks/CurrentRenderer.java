package org.auriferous.macrodeob.hooks;

import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class CurrentRenderer extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			AbstractInsnNode in = containsLDC(mn, "Toolkit ID: ");
			if (in != null) {
				InsnSearcher finder = new InsnSearcher(mn);
				FieldInsnNode fin = (FieldInsnNode) finder.searchBackwardSingle("getstatic", in);
				
				HooksMap.CLIENT_HOOKS_MAP.addClassHook("Renderer", getDesc(fin));
				HooksMap.CLIENT_HOOKS_MAP.addClientHook("CurrentRenderer", fin);
				return true;
			}
		}
		return false;
	}
	
}
