package org.auriferous.macrodeob.hooks;

import java.lang.reflect.Modifier;

import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.objectweb.asm.tree.FieldNode;


public class SoftReference extends Hook{

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (FieldNode fn : tcn.fields)
			if (!Modifier.isStatic(fn.access) && fn.desc.equals("Ljava/lang/ref/SoftReference;")) {
				ClassHook softReference = HooksMap.CLIENT_HOOKS_MAP.addClassHook("SoftReference", tcn.name);
				softReference.addFieldHook("CacheObject", fn, null);
			}
		return false;
	}
	
}
