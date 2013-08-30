package org.auriferous.macrodeob.hooks;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import org.auriferous.macrodeob.Main;
import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HookRecord;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class Player extends Hook {
	
	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (containsGameString(mn, "Self")) {
				InsnSearcher finder = new InsnSearcher(mn);
				List<AbstractInsnNode[]> results = finder.search("getfield");
				FieldInsnNode fin = (FieldInsnNode)results.get(8)[0];
				ClassHook player = HooksMap.CLIENT_HOOKS_MAP.addClassHook("Player", fin.owner);
				
				player.addFieldHook("CombatLevel", fin);
			}
		}
		int stringCount = 0;
		for (FieldNode fn : tcn.fields) {
			if (!Modifier.isStatic(fn.access)
					&& fn.desc.equals("Ljava/lang/String;")) {
				stringCount++;
			}
		}

		if (stringCount == 4) {
			MethodNode tM = null;
			for (MethodNode mn : tcn.methods) {
				if (!Modifier.isStatic(mn.access)
						&& mn.desc.endsWith("java/lang/String;")) {
					tM = mn;
					stringCount++;
				}
			}

			if (stringCount >= 5) {
				ClassHook player = HooksMap.CLIENT_HOOKS_MAP.addClassHook("Player", tcn);
				
				MethodInsnNode virt = null;
				for (MethodNode mn : tcn.methods) {
					if (!isStatic(mn) && mn.desc.startsWith("(L")) {
						boolean found = false;
						InsnSearcher finder = new InsnSearcher(mn);
						List<AbstractInsnNode[]> insns = finder.search("iload11 sipush if_icmplt");
						for (AbstractInsnNode[] result : insns) {
							if (((IntInsnNode)result[1]).operand == 16384) {
								ClassHook charHook = HooksMap.CLIENT_HOOKS_MAP.addClassHook("RSCharacter", tcn.superName);
								
								FieldInsnNode idleAnime = (FieldInsnNode) finder.searchBackwardSingle("getfield", mn.instructions.getLast());
								HookRecord idleHook = charHook.addFieldHook("Stationary", finder.searchBackwardSingle("putfield", mn.instructions.getLast()));
								idleHook.addLink(idleAnime);
								
								virt = (MethodInsnNode) finder.searchBackwardSingle("invokevirtual", mn.instructions.getLast(), 2);
								HookRecord hook = charHook.addMethodHook("IdleAnimation", virt);
								hook.addLink(idleAnime);
							}
						}
					} 
				}
				for (MethodNode mn : tcn.methods) {
					if (!isStatic(mn) && mn.desc.startsWith("(L") && mn.desc.endsWith(")Z")) {
						boolean found = false;
						InsnSearcher finder = new InsnSearcher(mn);
						List<AbstractInsnNode[]> insns = finder.search("iload2 istore4");
						if (!insns.isEmpty()) {
							ClassHook charHook = HooksMap.CLIENT_HOOKS_MAP.addClassHook("RSCharacter", tcn.superName);
							FieldInsnNode fin = (FieldInsnNode) finder.searchSingle("getfield");
							
							HookRecord hook = charHook.addMethodHook("Animation", virt);
							hook.addLink(fin);
						}
					}
				}
				
				for (FieldNode fn : tcn.fields) {
					if (!Modifier.isStatic(fn.access) && !isPrimitive(fn)) {
						ClassNode cn = Main.rsClassLoader.loadClass(getDesc(fn));
						ClassHook composite = HooksMap.CLIENT_HOOKS_MAP.addClassHook("PlayerComposite", cn);
						for (MethodNode mn : cn.methods) {
							if (!isStatic(mn) && mn.desc.startsWith("(II") && mn.desc.endsWith(")V")) {
								InsnSearcher finder = new InsnSearcher(mn);
								List<AbstractInsnNode[]> insns = finder.search("getfield iload. iload. (ldc|getstatic) ior iastore");
								if (insns.size() == 1) {
									player.addFieldHook("Composite", fn, null);
									composite.addFieldHook("Equipment", insns.get(0)[0]);
									//return true;
								}
							} else if (!isStatic(mn)) {
								InsnSearcher finder = new InsnSearcher(mn);
								List<AbstractInsnNode[]> results = finder.search("monitorenter");
								if (results.size() == 3) {
									HooksMap.CLIENT_HOOKS_MAP.addClientHook("PlayerModelCache", finder.searchSingle("getstatic", results.get(0)[0]));
									
									results = finder.search("getfield");
									for (AbstractInsnNode[] result : results) {
										if (((FieldInsnNode)result[0]).desc.equals("J")) {
											composite.addFieldHook("ModelID", result[0]);
											break;
										}
									}
								}
							}
						}
					}
				}
				return true;
			}
		}

		return false;
	}
	
	private boolean isPrimitive(FieldNode fn) {
		return fn.desc.equals("Ljava/lang/String;") || Type.getType(fn.desc).getSort() != Type.OBJECT;
	}

}
