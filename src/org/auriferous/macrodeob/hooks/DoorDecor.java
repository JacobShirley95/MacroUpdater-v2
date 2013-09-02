package org.auriferous.macrodeob.hooks;

import java.util.List;

import org.auriferous.macrodeob.Main;
import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class DoorDecor extends Hook{

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (!isStatic(mn) && mn.name.equals("<init>") && Type.getArgumentTypes(mn.desc).length < 16) {
				InsnSearcher finder = new InsnSearcher(mn);
				List<AbstractInsnNode[]> results = finder.search("iload[0-9]+ ldc ior istore[0-9]+");
				for (AbstractInsnNode[] result : results) {
					if ((int)((LdcInsnNode)result[1]).cst == 65536) {
						//System.out.println("DSFSDFvcvvcv");
						HooksMap.CLIENT_HOOKS_MAP.addClassHook("WallDecor", tcn.superName);
						ClassHook ch = HooksMap.CLIENT_HOOKS_MAP.addClassHook("DoorDecor", tcn);
						/*for (MethodNode mn2 : tcn.methods) {
							if (!isStatic(mn2) && !mn2.name.equals("<init>")) {
								finder = new InsnSearcher(mn2);
								results = finder.search("getfield iconst_3 aaload");
								if (!results.isEmpty()) {
									System.out.println("DSFSDF");
									ch.addFieldHook("Tiles", results.get(0)[0]);
									
									results = finder.search("getfield dup getfield iconst_1 isub i2b");
									FieldInsnNode fin = (FieldInsnNode) results.get(1)[0];
									ClassHook tile = HooksMap.CLIENT_HOOKS_MAP.addClassHook("Tile", fin.owner);
									tile.addFieldHook("WallDecor", fin);
								}
							}
						}*/
						FieldInsnNode f = (FieldInsnNode) finder.searchSingle("putfield", 1);
						for (MethodNode mn2 : tcn.methods)
							if (!isStatic(mn2)) {
								InsnSearcher finder2 = new InsnSearcher(mn2);
								List<AbstractInsnNode[]> results2 = finder2.search("getfield");
								if (results2.size() > 1) {
									FieldInsnNode f2 = (FieldInsnNode) results2.get(1)[0];
									if (f.name.equals(f2.name) && f.desc.equals(f2.desc) && f.owner.equals(f2.owner)) {
										results2 = finder2.search("iconst_0 aaload");
										if (!results2.isEmpty()) {
											System.out.println("DSFSDF");
											ch.addFieldHook("ID", f2);
											
											MethodInsnNode min = (MethodInsnNode) finder2.searchSingle("invokevirtual", results2.get(0)[0]);
											FieldInsnNode gamePos = (FieldInsnNode) finder2.searchSingle("getfield", min);
											
											ClassHook entity = HooksMap.CLIENT_HOOKS_MAP.addClassHook("Entity", Main.rsClassLoader.loadClass(tcn.superName).superName);
											entity.addFieldHook("WorldPos", gamePos).addLink(min);
											
											ClassHook gameCoord = HooksMap.CLIENT_HOOKS_MAP.addClassHook("GameCoord", getDesc(gamePos));
											
											results2 = finder2.search("getfield", gamePos.getNext());
											ch.addFieldHook("Plane", results2.get(0)[0]);
											
											gameCoord.addFieldHook("X", results2.get(2)[0]);
											gameCoord.addFieldHook("Y", results2.get(3)[0]);
											gameCoord.addFieldHook("Z", results2.get(4)[0]);
											
											break;
										}
									}
								}
							}
						ch.addFieldHook("Door", finder.searchSingle("putfield", 5));
						ch.addFieldHook("Model", finder.searchSingle("putfield", result[1]));
						
						return true;
					}
				}
			}
		}
		return false;
	}

}
