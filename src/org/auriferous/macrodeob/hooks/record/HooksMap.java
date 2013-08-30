package org.auriferous.macrodeob.hooks.record;

import java.util.HashMap;

import org.auriferous.macrodeob.Main;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class HooksMap {
	
	public static final HooksMap CLIENT_HOOKS_MAP = new HooksMap(); 
	
	public HashMap<String, ClassHook> classHooks = new HashMap<>();
	
	public ClassHook addClassHook(String hookName, FieldInsnNode field) {
		String t = null;
		if (field.getOpcode() == Opcodes.GETSTATIC)
			t = Type.getType(field.desc).getClassName().replaceAll("\\[\\]", "");
		else 
			t = getDeclaringClass(field).name;
				
		return addClassHook(hookName, t);
	}
	
	public ClassHook addClassHook(String hookName, ClassNode clazz) {
		return addClassHook(hookName, clazz.name);
	}
	
	public ClassHook addClassHook(String hookName, String className) {
		ClassHook ch = classHooks.get(className);
		if (ch != null)
			return ch;
		ch = new ClassHook(hookName, className);
		classHooks.put(className, ch);
		return ch;
	}
	
	public void addClientHook(String hookName, AbstractInsnNode in) {
		addClientHook(hookName, (FieldInsnNode)in);
	}
	
	public void addClientSetterHook(String hookName, AbstractInsnNode in) {
		FieldInsnNode fin = (FieldInsnNode)in;
		addClassHook("Client", "client").addStaticFieldSetterHook(hookName, fin);
	}
	
	public void addClientHook(String hookName, FieldInsnNode fin) {
		addClassHook("Client", "client").addStaticFieldHook(hookName, fin);
	}
	
	public HookRecord addClientHook(String hookName, MethodInsnNode min) {
		return addClassHook("Client", "client").addMethodHook(hookName, min);
	}
	
	public HookRecord addClientHook(String hookName, MethodInsnNode min, int paramCount) {
		return addClassHook("Client", "client").addMethodHook(hookName, min, paramCount);
	}
	
	public ClassHook getClassHook(String hookName) {
		return classHooks.get(hookName);
	}
	
	private ClassNode getDeclaringClass(FieldInsnNode fin) {
		return getDeclaringClass(Main.rsClassLoader.loadClass(fin.owner), fin);
	}
	
	private ClassNode getDeclaringClass(ClassNode cln, FieldInsnNode fin) {
		for (FieldNode fn : cln.fields) 
			if (fn.name.equals(fin.name) && fn.desc.equals(fin.desc))
				return cln;
		return getDeclaringClass(Main.rsClassLoader.loadClass(cln.superName), fin);
	}
}
