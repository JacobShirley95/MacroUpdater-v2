package org.auriferous.macrodeob.hooks.injector;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.auriferous.macrodeob.Main;
import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HookRecord;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.utils.InsnUtils;
import org.auriferous.macrodeob.utils.RsClassLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class Injector {
	private String root;
	private HooksMap hooks;
	private RsClassLoader classLoader;
	public Injector(RsClassLoader classLoader, HooksMap mapping) {
		this.hooks = mapping;
		this.root = null;
		this.classLoader = classLoader;
	}

	public Injector(RsClassLoader classLoader, HooksMap mapping, String interfacesRoot) {
		this.hooks = mapping;
		this.root = interfacesRoot;
		this.classLoader = classLoader;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getRoot() {
		return root;
	}

	public void inject(ClassHook ch, String root) {
		ClassNode cn = classLoader.loadClass(ch.name);
		cn.interfaces.add(root + ch.hookName);
		for (HookRecord fh : ch.hooks.values()) {
			MethodNode mn = createGetter(fh, root);
			cn.methods.add(mn);
		}
	}

	public void injectAll() {
		for (ClassHook ch : hooks.classHooks.values())
			inject(ch, this.root);
	}

	public void injectAll(String root) {
		for (ClassHook ch : hooks.classHooks.values())
			inject(ch, root);
	}

	private String renameDesc(String desc, String root) {
		Type t = Type.getType(desc);
		switch (t.getSort()) {
		case Type.ARRAY:
			String s = renameDesc(t.getElementType().getDescriptor(), root);
			for (int i = 0; i < t.getDimensions(); ++i) {
				s = '[' + s;
			}
			return s;
		case Type.OBJECT:
			ClassHook ch = hooks.classHooks.get(t.getInternalName());
			if (ch != null) {
				return "L"+root + ch.hookName + ';';
			}
		}
		return desc;
	}
	
	private String reverseDesc(String desc, String root) {
		Type t = Type.getType(desc);
		switch (t.getSort()) {
		case Type.ARRAY:
			String s = renameDesc(t.getElementType().getDescriptor(), root);
			for (int i = 0; i < t.getDimensions(); ++i) {
				s = '[' + s;
			}
			return s;
		case Type.OBJECT:
			String s2 = null;
			for (Entry<String, ClassHook> ch : hooks.classHooks.entrySet()) {
				if (ch.getValue().hookName.equals(t.getInternalName())) {
					s2 = ch.getKey();
					break;
				}
			}
			if (s2 != null) {
				return "L"+root + s2 + ';';
			}
		}
		return desc;
	}

	private MethodNode createGetter(HookRecord hook, String root) {
		String desc = hook.isMethod ? renameDesc(Type.getReturnType(hook.desc).getDescriptor(), root) : renameDesc(hook.desc, root);
		
		String type = "";
		if (!hook.isSetter && hook.paramCount == 0)
			type = desc.equals("Z") ? "is" : "get";
		
		
		MethodNode method = new MethodNode(Modifier.PUBLIC, type + hook.hookName, "()"+desc, "()"+desc, new String[] {});
		//method.access = hook.isStatic ? method.access|Modifier.STATIC : 0;
		hook.chain.add(hook);

		int paramOffset = 1;
		List<Type> mArgs = new ArrayList<>();
		int c = 0;
		for (HookRecord record : hook.chain) {
			if (c == 0 && !record.isStatic)
				method.visitVarInsn(Opcodes.ALOAD, 0);
			c++;
			if (record.isMethod) {
				Type[] args = Type.getArgumentTypes(record.desc);
				int len = record.modifier != null ? args.length-1 : args.length;
				for (int i = 0; i < len; i++) {
					int t = args[i].getSort();
					int op = -1;
					boolean cast = false;
					if (t == Type.INT || t == Type.BYTE || t == Type.SHORT || t == Type.BOOLEAN)
						op = Opcodes.ILOAD;
					else if (t == Type.DOUBLE)
						op = Opcodes.DLOAD;
					else if (t == Type.LONG)
						op = Opcodes.LLOAD;
					else if (t == Type.FLOAT)
						op = Opcodes.FLOAD;
					else if (t == Type.ARRAY)
						op = Opcodes.ALOAD;
					else {
						op = Opcodes.ALOAD;
						if (Main.rsClassLoader.loadClass(args[i].getInternalName()) != null)
							cast = true;
					}
					
					method.visitVarInsn(op, paramOffset++);
					if (cast)
						method.visitTypeInsn(Opcodes.CHECKCAST, args[i].getClassName());
					mArgs.add(Type.getType(renameDesc(args[i].getDescriptor(), root)));
				}
					
				if (record.modifier != null) {
					if (!(record.modifier instanceof Integer)) {
						method.visitLdcInsn(record.modifier);
					} else {
						int mod = (int)record.modifier;
					if (mod <= 5 && mod >= -1)
						method.visitInsn(mod+3);
					else if (mod >= Byte.MIN_VALUE && mod <= Byte.MAX_VALUE) {
						method.visitIntInsn(Opcodes.BIPUSH, mod);
					} else if (mod >= Short.MIN_VALUE && mod <= Short.MAX_VALUE)
						method.visitIntInsn(Opcodes.SIPUSH, mod);
					else 
						method.visitLdcInsn(record.modifier);
					}	
				}
				
				int op = -1;
				if (record.isStatic)
					op = Opcodes.INVOKESTATIC;
				else
					op = Opcodes.INVOKEVIRTUAL;
				method.visitMethodInsn(op, record.owner, record.name,
						record.desc);
				
			} else {
				if (record.isSetter && record.modifier != null) 
					record.modifier = ClassHook.inverse(record.modifier);
					
				if (record.isSetter) {
					if (record.modifier != null) {
						if (!(record.modifier instanceof Integer)) {
							method.visitLdcInsn(record.modifier);
						} else {
							int mod = (int)record.modifier;
						if (mod <= 5 && mod >= -1)
							method.visitInsn(mod+3);
						else if (mod >= Byte.MIN_VALUE && mod <= Byte.MAX_VALUE) {
							method.visitIntInsn(Opcodes.BIPUSH, mod);
						} else if (mod >= Short.MIN_VALUE && mod <= Short.MAX_VALUE)
							method.visitIntInsn(Opcodes.SIPUSH, mod);
						else 
							method.visitLdcInsn(record.modifier);
						}	
					}
					int t = Type.getType(record.desc).getSort();
					
					int op = -1;
					if (t == Type.INT || t == Type.BYTE || t == Type.SHORT || t == Type.BOOLEAN)
						op = Opcodes.ILOAD;
					else if (t == Type.DOUBLE)
						op = Opcodes.DLOAD;
					else if (t == Type.LONG)
						op = Opcodes.LLOAD;
					else if (t == Type.FLOAT)
						op = Opcodes.FLOAD;
					else
						op = Opcodes.ALOAD;
					method.visitVarInsn(op, paramOffset++);
					if (record.modifier != null) {
						
						if (t == Type.INT || t == Type.BYTE || t == Type.SHORT || t == Type.BOOLEAN)
							method.visitInsn(Opcodes.IMUL);
						else if (t == Type.DOUBLE)
							method.visitInsn(Opcodes.DMUL);
						else if (t == Type.LONG)
							method.visitInsn(Opcodes.LMUL);
						else if (t == Type.FLOAT)
							method.visitInsn(Opcodes.FMUL);
					}
					
					method.visitFieldInsn(record.isStatic ? Opcodes.PUTSTATIC
							: Opcodes.PUTFIELD, record.owner, record.name,
							record.desc);
					mArgs.add(Type.getType(reverseDesc(record.desc, root)));
				} else {
					method.visitFieldInsn(record.isStatic ? Opcodes.GETSTATIC
							: Opcodes.GETFIELD, record.owner, record.name,
							record.desc);
					
					if (record.modifier != null) {
						if (!(record.modifier instanceof Integer)) {
							method.visitLdcInsn(record.modifier);
						} else {
							int mod = (int)record.modifier;
						if (mod <= 5 && mod >= -1)
							method.visitInsn(mod+3);
						else if (mod >= Byte.MIN_VALUE && mod <= Byte.MAX_VALUE) {
							method.visitIntInsn(Opcodes.BIPUSH, mod);
						} else if (mod >= Short.MIN_VALUE && mod <= Short.MAX_VALUE)
							method.visitIntInsn(Opcodes.SIPUSH, mod);
						else 
							method.visitLdcInsn(record.modifier);
						}	
					}

				if (record.modifier != null) {
					int t = Type.getType(record.desc).getSort();
					if (t == Type.INT || t == Type.BYTE || t == Type.SHORT)
						method.visitInsn(Opcodes.IMUL);
					else if (t == Type.DOUBLE)
						method.visitInsn(Opcodes.DMUL);
					else if (t == Type.LONG)
						method.visitInsn(Opcodes.LMUL);
					else if (t == Type.FLOAT)
						method.visitInsn(Opcodes.FMUL);
				}
				}
			}
		}
		
		if (hook.isSetter) {
			method.visitInsn(Opcodes.RETURN);
		} else {
		int t = Type.getType(desc).getSort();
		if (t == Type.ARRAY || t == Type.OBJECT)
			method.visitInsn(Opcodes.ARETURN);
		else if (t == Type.DOUBLE)
			method.visitInsn(Opcodes.DRETURN);
		else if (t == Type.LONG)
			method.visitInsn(Opcodes.LRETURN);
		else if (t == Type.FLOAT)
			method.visitInsn(Opcodes.FRETURN);
		else if (t == Type.VOID)
			method.visitInsn(Opcodes.RETURN);
		else
			method.visitInsn(Opcodes.IRETURN);
		}
		
		//if (hook.isMethod) {
			Type[] ts = new Type[mArgs.size()];
			desc = Type.getMethodDescriptor(hook.isSetter ? Type.VOID_TYPE : Type.getType(desc), mArgs.toArray(ts));
			method.desc = method.signature = desc;
		//}

		return method;
	}
}
