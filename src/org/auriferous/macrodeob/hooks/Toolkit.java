package org.auriferous.macrodeob.hooks;

import org.auriferous.macrodeob.Main;
import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class Toolkit extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			AbstractInsnNode in = containsLDC(mn, "Toolkit ID: ");
			if (in != null) {
				InsnSearcher finder = new InsnSearcher(mn);
				
				FieldInsnNode fin = (FieldInsnNode)finder.searchSingle("getstatic", in);
				HooksMap.CLIENT_HOOKS_MAP.addClientHook("RenderSettings", fin);
				
				ClassHook settings = HooksMap.CLIENT_HOOKS_MAP.addClassHook("RenderSettings", getDesc(fin));
				fin = (FieldInsnNode) finder.searchSingle("getfield", in);
				settings.addFieldHook("Toolkit", fin);
				
				ClassHook toolkit = HooksMap.CLIENT_HOOKS_MAP.addClassHook("Toolkit", getDesc(fin));
				
				MethodInsnNode min = (MethodInsnNode)finder.searchSingle("invokevirtual", fin);
				
				MethodNode mn2 = getMethod(min);
				finder = new InsnSearcher(mn2);
				toolkit.addFieldHook("ID", finder.searchSingle("getfield"));
				
				return true;
			}
		}
		return false;
	}

}
