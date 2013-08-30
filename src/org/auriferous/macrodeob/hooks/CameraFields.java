package org.auriferous.macrodeob.hooks;

import java.util.List;

import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class CameraFields extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (isStatic(mn) && mn.desc.startsWith("(L")
					&& mn.desc.endsWith(")V")) {
				AbstractInsnNode in = containsLDC(mn, 2607.5945876176133);
				if (in != null) {
					InsnSearcher finder = new InsnSearcher(mn);
					if (finder.search("bipush ishr").size() == 2) {
						FieldInsnNode fin = (FieldInsnNode) finder.searchBackwardSingle("getstatic", in);
						if (fin.desc.equals("I")) {
							fin = (FieldInsnNode) finder.searchSingle("getstatic", in);
						}
						MethodInsnNode min = (MethodInsnNode) finder.searchSingle("invokevirtual", fin);
						HooksMap.CLIENT_HOOKS_MAP.addClientHook("CameraAngle", min).addLink(fin);
						HooksMap.CLIENT_HOOKS_MAP.addClientHook("CameraOrigin", finder.searchSingle("getstatic", min));
					
						return true;
					}
					//return true;
				}
			}
		}
		return false;
	}

}
