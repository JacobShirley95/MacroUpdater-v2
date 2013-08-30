package org.auriferous.macrodeob.hooks;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.auriferous.macrodeob.Main;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.transformers.miners.GameString;
import org.auriferous.macrodeob.utils.InsnUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class Hook {
	private boolean active = true;

	public boolean dependenciesMet() {
		return true;
	}
	
	public final void setActive(boolean flag) {
		this.active = flag;
	}
	
	public final boolean isActive() {
		return active;
	}
	
	protected boolean isStatic(MethodNode mn) {
		return Modifier.isStatic(mn.access);
	}
	
	protected AbstractInsnNode containsLDC(MethodNode mn, Object ldc) {
			for (AbstractInsnNode in : mn.instructions.toArray())
				if (in instanceof LdcInsnNode)
					if (((LdcInsnNode)in).cst.equals(ldc))
						return in;
		return null;
	}
	
	protected int countFieldsWithDescs(ClassNode cn, String... descs) {
		int count = 0;
		for (FieldNode fn : cn.fields) {
			for (String desc : descs)
				if (!Modifier.isStatic(fn.access) && fn.desc.equals(desc))
					count++;
		}
		return count;
	}
	
	protected int countClassMethods(ClassNode cn) {
		int count = 0;
		for (MethodNode mn : cn.methods)
			if (!isStatic(mn) && !mn.name.equals("<clinit>"))
				count++;
		return count;
	}
	
	protected MethodNode getMethodConstructor(ClassNode cn) {
		for (MethodNode mn : cn.methods) {
			if (mn.name.equals("<init>")) {
				return mn;
			}
		}
		return null;
	}
	
	protected List<MethodNode> getMethodConstructors(ClassNode cn) {
		List<MethodNode> list = new ArrayList<>();
		for (MethodNode mn : cn.methods) {
			if (mn.name.equals("<init>")) {
				list.add(mn);
			}
		}
		return list.size() == 0 ? null : list;
	}
	
	protected List<MethodNode> getMethodsWithDesc(ClassNode cn, String desc) {
		List<MethodNode> list = new ArrayList<>();
		for (MethodNode mn : cn.methods) {
			if (!isStatic(mn) && mn.desc.matches(desc)) {
				list.add(mn);
			}
		}
		return list.size() == 0 ? null : list;
	}
	
	protected List<MethodNode> getMethodsWithName(ClassNode cn, String name) {
		List<MethodNode> list = new ArrayList<>();
		for (MethodNode mn : cn.methods) {
			if (!isStatic(mn) && mn.name.matches(name)) {
				list.add(mn);
			}
		}
		return list.size() == 0 ? null : list;
	}
	
	protected boolean containsGameString(MethodNode mn, String str) {
		for (AbstractInsnNode in : mn.instructions.toArray()) {
			if (in.getOpcode() == Opcodes.GETSTATIC) {
				FieldInsnNode fin = (FieldInsnNode)in;
				String s = GameString.stringMap.get(fin.owner+"."+fin.name);
				if (s != null && s.equals(str)) 
					return true;
			}
		}
		return false;
	}
	
	protected AbstractInsnNode containsGameInt(MethodNode mn, String intStr) {
		for (AbstractInsnNode in : mn.instructions.toArray()) {
			if (in.getOpcode() == Opcodes.GETSTATIC) {
				FieldInsnNode fin = (FieldInsnNode)in;
				String s = GameString.intMap.get(fin.owner+"."+fin.name);
				if (s != null && s.equals(intStr)) 
					return in;
			}
		}
		return null;
	}
	
	protected String getDesc(FieldInsnNode fin) {
		return Type.getType(fin.desc).getClassName().replaceAll("\\[\\]", "");
	}
	
	protected String getDesc(FieldNode fn) {
		return Type.getType(fn.desc).getClassName().replaceAll("\\[\\]", "");
	}
	
	protected MethodNode getMethod(MethodInsnNode min) {
		return ((TransformClassNode)Main.rsClassLoader.loadClass(min.owner)).findMethod(min.name, min.desc);
	}
	
	protected boolean containsNumbers(MethodNode mn, int... numbers) {
		Set<Integer> nums = new HashSet<>();
		for (AbstractInsnNode in : mn.instructions.toArray()) {
			if (isNumberInsn(in)) {
				int oper = getNumber(in);
				for (int num : numbers) 
					if (oper == num)
						nums.add(num);
				if (nums.size() == numbers.length)
					return true;
			}
		}
		return false;
	}
	
	private int getNumber(AbstractInsnNode in) {
		if (in instanceof IntInsnNode)
			return ((IntInsnNode)in).operand;
		else return in.getOpcode()-3;
	}
	
	private boolean isNumberInsn(AbstractInsnNode in) {
		return (in instanceof IntInsnNode) || InsnUtils.inRange(in.getOpcode(), Opcodes.ICONST_M1, Opcodes.ICONST_5);
	}

	public abstract boolean accept(TransformClassNode tcn);
}
