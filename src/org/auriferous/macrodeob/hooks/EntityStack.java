package org.auriferous.macrodeob.hooks;

import java.util.List;

import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class EntityStack extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (isStatic(mn) && mn.instructions.size() > 0
					&& mn.desc.startsWith("(I")) {
				LdcInsnNode ldc = (LdcInsnNode) containsLDC(mn, "Failure");
				if (ldc != null) {
					InsnSearcher finder = new InsnSearcher(mn);

					FieldInsnNode getstatic = (FieldInsnNode) finder
							.searchSingle("getstatic", 1);
					MethodInsnNode invoke = (MethodInsnNode) finder
							.searchSingle("invokevirtual", getstatic);

					HooksMap.CLIENT_HOOKS_MAP.addClientHook("EntityStack",
							invoke).addLink(getstatic);
					HooksMap.CLIENT_HOOKS_MAP.addClassHook("WorldObjects",
							getDesc(invoke));

					return true;
				}
			}
		}
		return false;
	}

}
