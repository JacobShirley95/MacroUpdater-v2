package org.auriferous.macrodeob.hooks;

import java.util.List;

import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class Viewport extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (containsLDC(mn, " - ") != null) {
				InsnSearcher finder = new InsnSearcher(mn);
				List<AbstractInsnNode[]> results = finder.search("getfield");
				
				ClassHook transform3d = HooksMap.CLIENT_HOOKS_MAP.addClassHook("Viewport", tcn);
				transform3d.addFieldHook("Float1", results.get(0)[0]);
				transform3d.addFieldHook("Float2", results.get(1)[0]);
				transform3d.addFieldHook("Float3", results.get(2)[0]);
				transform3d.addFieldHook("Float4", results.get(3)[0]);
				transform3d.addFieldHook("Float5", results.get(4)[0]);
				transform3d.addFieldHook("Float6", results.get(5)[0]);
				transform3d.addFieldHook("Float7", results.get(6)[0]);
				transform3d.addFieldHook("Float8", results.get(7)[0]);
				transform3d.addFieldHook("Float9", results.get(8)[0]);
				transform3d.addFieldHook("Float10", results.get(9)[0]);
				transform3d.addFieldHook("Float11", results.get(10)[0]);
				transform3d.addFieldHook("Float12", results.get(11)[0]);				
				
				return true;
			}
		}
		return false;
	}

}
