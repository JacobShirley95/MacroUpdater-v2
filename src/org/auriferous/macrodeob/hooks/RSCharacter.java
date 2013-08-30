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
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.Type;

public class RSCharacter extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {

		int stringCount = 0;
		for (FieldNode fn : tcn.fields) {
			if (!Modifier.isStatic(fn.access)
					&& fn.desc.equals("Ljava/lang/String;")) {
				stringCount++;
			}
		}

		if (stringCount == 4) {
			for (MethodNode mn : tcn.methods) {
				if (!Modifier.isStatic(mn.access)
						&& mn.desc.endsWith("java/lang/String;")) {
					stringCount++;
				}
			}

			if (stringCount >= 5) {
				tcn = Main.rsClassLoader.loadClass(tcn.superName);
				ClassHook animeHook = HooksMap.CLIENT_HOOKS_MAP.addClassHook(
						"RSCharacter", tcn);
				List<MethodNode> mns = getMethodConstructors(tcn);
				if (mns != null) {
					for (MethodNode mn : mns) {
						InsnSearcher finder = new InsnSearcher(mn);
						List<AbstractInsnNode[]> results = finder
								.search("aload0 iconst_5 anewarray putfield");
						if (!results.isEmpty()) {
							FieldInsnNode fin = (FieldInsnNode) results.get(0)[3];
							String s = Type.getType(fin.desc).getClassName().replaceAll("\\[\\]", "");
							
							HooksMap.CLIENT_HOOKS_MAP.addClassHook("Model", s);
							animeHook.addFieldHook("Models", fin);
						}
					}
				}
				for (MethodNode mn : tcn.methods) {
					if (mn.instructions.size() == 0)
						continue;
					
					if (!isStatic(mn) && mn.desc.startsWith("(I") && mn.desc.endsWith(")Z")  && mn.instructions.size() < 50 && mn.instructions.size() > 30) {
						InsnSearcher finder = new InsnSearcher(mn);
						List<AbstractInsnNode[]> results = finder.search("putfield aload0 getfield getfield");
						if (!results.isEmpty()) {
							animeHook.addFieldHook("HeadInterfaces", results.get(0)[2]);
						}
					}
					if (!isStatic(mn) && mn.desc.endsWith(")I")) {
						InsnSearcher finder = new InsnSearcher(mn);
						List<AbstractInsnNode[]> results = finder.search("putfield aload0 getfield getfield");
						if (!results.isEmpty()) {
							animeHook.addFieldHook("HeadInterfaces", results.get(0)[2]);
						}
					}
				}
				return true;
			}
		}

		return false;
	}

}
