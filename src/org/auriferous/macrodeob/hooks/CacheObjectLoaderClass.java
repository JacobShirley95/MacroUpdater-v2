package org.auriferous.macrodeob.hooks;

import java.lang.reflect.Modifier;
import java.util.List;

import org.auriferous.macrodeob.Main;
import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class CacheObjectLoaderClass extends Hook{

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (mn.name.equals("<init>") && containsGameString(mn, "Take")) {
				for (MethodNode mn2 : tcn.methods) {
					String desc = mn2.desc;
					int len = Type.getArgumentTypes(desc).length;
					if (!isStatic(mn2) && len <= 3 && desc.contains(")L")) {
						InsnSearcher finder = new InsnSearcher(mn2);
						
						if (finder.search("monitorenter").isEmpty())
							continue;
						
						
						List<AbstractInsnNode[]> results = finder.search("getfield");
						
						FieldInsnNode itemCache = (FieldInsnNode)results.get(0)[0];
						ClassHook cacheObjectLoader = HooksMap.CLIENT_HOOKS_MAP.addClassHook("CacheObjectLoader", tcn);
						cacheObjectLoader.addFieldHook("ItemCache", itemCache);
						
						results = finder.search("new dup invokespecial astore[0-9]*");
						results = finder.search("putfield", results.get(0)[0]);
						
						FieldInsnNode fin = (FieldInsnNode)results.get(1)[0];
						ClassHook objectDescriptor = HooksMap.CLIENT_HOOKS_MAP.addClassHook("RSObject", fin);

						objectDescriptor.addFieldHook("ID", fin);
						//objectDescriptor.addFieldHook("ScreenOptions", (FieldInsnNode)results.get(2)[0]);
						objectDescriptor.addFieldHook("InvOptions", (FieldInsnNode)results.get(3)[0]);
						objectDescriptor.addFieldHook("NoteID", finder.searchSingle("getfield", results.get(3)[0]));
						
						MethodInsnNode min = (MethodInsnNode)finder.searchSingle("invokevirtual");
						MethodNode mn3 = getMethod(min);
						
						finder = new InsnSearcher(mn3);
					
						ClassHook cacheNodeList = HooksMap.CLIENT_HOOKS_MAP.addClassHook("CacheNodeList", getDesc((FieldInsnNode)itemCache));
						cacheNodeList.addFieldHook("List", (FieldInsnNode)finder.searchSingle("getfield"));
						
						results = finder.search("new");
						
						TransformClassNode cacheNodeClass = Main.rsClassLoader.loadClass(((TypeInsnNode)results.get(0)[0]).desc);
						ClassHook cacheNode = HooksMap.CLIENT_HOOKS_MAP.addClassHook("CacheNode", cacheNodeClass);
						
						for (FieldNode fn : cacheNodeClass.fields)
							if (!Modifier.isStatic(fn.access) && fn.desc.equals("Ljava/lang/Object;"))
								cacheNode.addFieldHook("CacheObject", fn, null);
						
						return true;
 					}
				}
			}
		}
		return false;
	}
	
}
