package org.auriferous.macrodeob.hooks;

import java.lang.reflect.Modifier;
import java.util.List;

import org.auriferous.macrodeob.Main;
import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class NPC extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {
		int stringCount = 0;
		FieldNode name = null;
		for (FieldNode fn : tcn.fields) {
			if (!Modifier.isStatic(fn.access)
					&& fn.desc.equals("Ljava/lang/String;")) {
				name = fn;
				stringCount++;
			}
		}

		if (stringCount == 1) {
			List<MethodNode> constructors = getMethodConstructors(tcn);
			if (constructors.size() == 2) {
				for (MethodNode constructor : constructors) {
					InsnSearcher finder = new InsnSearcher(constructor);
					List<AbstractInsnNode[]> results = finder
							.search("aload[0-9]* bipush newarray putfield");
					if (results.size() == 2) {
						for (AbstractInsnNode[] result : results) {
							if (((IntInsnNode) result[1]).operand == 6) {
								ClassHook npc = HooksMap.CLIENT_HOOKS_MAP
										.addClassHook("NPC", tcn);
								npc.addFieldHook("Name", name, null);

								for (MethodNode mn : tcn.methods) {
									if (!isStatic(mn) && mn.desc.contains("ZZ")) {
										finder = new InsnSearcher(mn);
										List<AbstractInsnNode[]> results2 = finder
												.search("getfield getfield");
										if (!results2.isEmpty()) {
											FieldInsnNode fin = (FieldInsnNode) results2
													.get(0)[0];
											FieldInsnNode fin2 = (FieldInsnNode) results2
													.get(0)[1];
											if (fin.owner.equals(tcn.name)
													&& fin2.desc
															.equals("Ljava/lang/String;")) {
												npc.addFieldHook(
														"NPCComposite", fin);

												ClassHook composite = HooksMap.CLIENT_HOOKS_MAP
														.addClassHook(
																"NPCComposite",
																getDesc(fin));
												composite.addFieldHook("Name",
														fin2);
												FieldInsnNode cbLevel = (FieldInsnNode) results2
														.get(1)[1];

												exploreComposite(composite,
														cbLevel);
												return true;
											}
										}
									}
								}
							}

							return true;
						}
					}
				}
			}
		}

		return false;
	}

	private void exploreComposite(ClassHook hook, FieldInsnNode cbLevel) {
		ClassNode cn = Main.rsClassLoader.loadClass(hook.name);
		for (FieldNode fn : cn.fields) {
			if (!Modifier.isStatic(fn.access)
					&& fn.desc.equals("[Ljava/lang/String;")) {
				hook.addFieldHook("Actions", fn, null);
				break;
			}
		}
		for (MethodNode mn : cn.methods) {
			if (!isStatic(mn) && !mn.name.equals("<init>")) {
				InsnSearcher finder = new InsnSearcher(mn);
				List<AbstractInsnNode[]> results = finder.search("putfield");
				for (AbstractInsnNode[] result : results) {
					FieldInsnNode fin = (FieldInsnNode) result[0];
					if (fin.owner.equals(cbLevel.owner)
							&& fin.name.equals(cbLevel.name)
							&& fin.desc.equals(cbLevel.desc)) {
						hook.addFieldHook("CombatLevel", fin);
						break;
					}
				}
			}
		}
		for (MethodNode mn : cn.methods) {
			if (!isStatic(mn) && mn.desc.endsWith(";")) {
				InsnSearcher finder = new InsnSearcher(mn);
				if (!finder.search("monitorenter").isEmpty()) {
					List<AbstractInsnNode[]> results = finder
							.search("bipush ishl");
					if (!results.isEmpty()) {
						FieldInsnNode fin = (FieldInsnNode) finder
								.searchBackwardSingle("getfield",
										results.get(0)[0]);

						HooksMap.CLIENT_HOOKS_MAP.addClassHook("Renderer",
								fin.owner).addFieldHook("RendererID", fin);
						hook.addFieldHook("ModelID",
								finder.searchBackwardSingle("getfield", fin));
						break;
					}
				}
			}
		}
	}
}
