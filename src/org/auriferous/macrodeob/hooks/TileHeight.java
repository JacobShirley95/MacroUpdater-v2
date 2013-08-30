package org.auriferous.macrodeob.hooks;

import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class TileHeight extends Hook{

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			LdcInsnNode ldc = (LdcInsnNode) containsLDC(mn, " Height: ");
			if (ldc != null) {
				MethodInsnNode min = (MethodInsnNode) new InsnSearcher(mn).searchSingle("invokestatic", ldc);
				
				HooksMap.CLIENT_HOOKS_MAP.addClientHook("getTileHeight", min, 3);
				
				return true;
			}
		}
		return false;
	}

}
