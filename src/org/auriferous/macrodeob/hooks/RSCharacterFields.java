package org.auriferous.macrodeob.hooks;

import java.lang.reflect.Modifier;
import java.util.List;

import org.auriferous.macrodeob.Main;
import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.Type;

public class RSCharacterFields extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (isStatic(mn) && mn.desc.startsWith("(IIIIII") && mn.instructions.size() > 0) {
				InsnSearcher finder = new InsnSearcher(mn);
				List<AbstractInsnNode[]> results = finder.search(".ipush if_icmpge");
				int count = 0;
				for (AbstractInsnNode[] result : results)
					if (((IntInsnNode)result[0]).operand == 150)
						count++;
				if (count == 3) {
					results = finder.search("fcmpl");
					if (!results.isEmpty()) {
						MethodInsnNode min = (MethodInsnNode) finder.searchBackwardSingle("invokevirtual", results.get(0)[0]);
		
						ClassHook animeHook = HooksMap.CLIENT_HOOKS_MAP.addClassHook("RSCharacter", min.owner);
						animeHook.addMethodHook("Height", min);
						
						AbstractInsnNode in = finder.searchSingle("getstatic", results.get(0)[0]);
						HooksMap.CLIENT_HOOKS_MAP.addClientSetterHook("setPositionArray", in);
						HooksMap.CLIENT_HOOKS_MAP.addClientHook("PositionArrayData", in);
						HooksMap.CLIENT_HOOKS_MAP.addClientHook("PositionArray", (MethodInsnNode)finder.searchBackwardSingle("invokestatic", results.get(0)[0]));
						
						return true;
					}
				}
			}
		}

		return false;
	}

}
