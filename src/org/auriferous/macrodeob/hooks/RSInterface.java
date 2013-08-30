package org.auriferous.macrodeob.hooks;

import java.util.Collections;
import java.util.List;

import org.auriferous.macrodeob.Main;
import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class RSInterface extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (isStatic(mn) && mn.desc.startsWith("([L") && Type.getArgumentTypes(mn.desc).length >= 10) {
				InsnSearcher finder = new InsnSearcher(mn);
				List<AbstractInsnNode[]> list = finder.search("invokevirtual");
				for (AbstractInsnNode[] insns : list) {
					if (((MethodInsnNode)insns[0]).name.equals("setBounds")) {					
						List<AbstractInsnNode[]> results = finder.searchBackward("getfield", insns[0]);
						
						Collections.reverse(results);
						FieldInsnNode fin = (FieldInsnNode)results.get(0)[0];
						ClassHook rsInterface = HooksMap.CLIENT_HOOKS_MAP.addClassHook("RSInterface", fin);
						
						rsInterface.addFieldHook("ParentID", finder.searchSingle("getfield"));
						rsInterface.addFieldHook("Height", fin);
						rsInterface.addFieldHook("Width", (FieldInsnNode)results.get(1)[0]);
						rsInterface.addFieldHook("Y", (FieldInsnNode)results.get(2)[0]);
						rsInterface.addFieldHook("X", (FieldInsnNode)results.get(3)[0]);
						AbstractInsnNode from = results.get(3)[0];		
						
						results = finder.search("getfield", fin);
						rsInterface.addFieldHook("HoverColour", (FieldInsnNode)results.get(4)[0]);
						
						results = finder.search("invokestatic", from);
						for (AbstractInsnNode[] result : results) {
							MethodInsnNode min = ((MethodInsnNode)result[0]);
							if (min.desc.endsWith(")Z")) {
								MethodNode mn2 = getMethod(min);
								InsnSearcher finder2 = new InsnSearcher(mn2);
								rsInterface.addFieldHook("Hidden", finder2.searchSingle("getfield", 2));
							} else if (min.owner.equals(tcn.name) && min.name.equals(mn.name) && min.desc.equals(mn.desc)) {
								HooksMap.CLIENT_HOOKS_MAP.addClientHook("Widgets", finder.searchSingle("getstatic", result[0]));
								
								TypeInsnNode cast = (TypeInsnNode)finder.searchSingle("checkcast", result[0]);
								FieldInsnNode widgetNode = (FieldInsnNode)finder.searchSingle("getfield", cast);
								HooksMap.CLIENT_HOOKS_MAP.addClassHook("WidgetNode", cast.desc).addFieldHook("WidgetID", widgetNode);
								
								results = finder.searchBackward("getfield", result[0]);
								
								int size = results.size();
								rsInterface.addFieldHook("Index", results.get(size-5)[0]);
								rsInterface.addFieldHook("Type", results.get(size-4)[0]);
								rsInterface.addFieldHook("ID", results.get(size-3)[0]);
								rsInterface.addFieldHook("ScrollX", results.get(size-2)[0]);
								rsInterface.addFieldHook("ScrollY", results.get(size-1)[0]);
								break;
							}
						}
						
						results = finder.search("ldc", insns[0]);
						for (AbstractInsnNode[] result : results) {
							if (((LdcInsnNode)result[0]).cst.toString().equals("null")) {
								results = finder.searchBackward("getfield", result[0]);
								rsInterface.addFieldHook("ItemID", results.get(results.size()-2)[0]);
								break;
							}
						}
						
						results = finder.search("getfield", insns[0]);
						
						for (int i = 0; i < results.size(); i++) {
							AbstractInsnNode[] result = results.get(i);
							FieldInsnNode fin2 = (FieldInsnNode)result[0];
							if (fin2.desc.equals("Ljava/lang/String;") && fin2.owner.equals(fin.owner)) {
								rsInterface.addFieldHook("Outline", finder.searchBackwardSingle("getfield", result[0]));
								rsInterface.addFieldHook("Title", (FieldInsnNode)result[0]);
								
								results = finder.search("sipush", result[0]);
								for (AbstractInsnNode[] result2 : results)
									if (((IntInsnNode)result2[0]).operand == 4096) {
										rsInterface.addFieldHook("Rotation", finder.searchSingle("getfield", result2[0], 0));
										break;
									}
								
								
								break;
							} else if (fin2.desc.equals("[L"+fin.owner+";")) {
								rsInterface.addFieldHook("Children", (FieldInsnNode)result[0]);
							}
						}
						
						AbstractInsnNode node = containsLDC(mn, " x");
						rsInterface.addFieldHook("ItemAmount",  finder.searchSingle("getfield", node));
						
						ClassNode interfaceClass = Main.rsClassLoader.loadClass(fin.owner);
						for (FieldNode fn : interfaceClass.fields)
							if (fn.desc.equals("[Ljava/lang/String;"))
								rsInterface.addFieldHook("Actions",  fn, null);
						
						for (MethodNode mn2 : interfaceClass.methods)
							if (!isStatic(mn2) && mn2.desc.startsWith("(L") && mn2.desc.contains(")L")) {
								finder = new InsnSearcher(mn2);
								results = finder.search("bipush lshl");
								for (AbstractInsnNode[] result : results) {
									if (((IntInsnNode)result[0]).operand == 39) {
										from = finder.searchSingle("invokestatic");
										rsInterface.addFieldHook("ImageID", finder.searchBackwardSingle("getfield", from));
										break;
									}
								}
							}
						
						return true;
					}
				}
			}
		}
		return false;
	}

}
