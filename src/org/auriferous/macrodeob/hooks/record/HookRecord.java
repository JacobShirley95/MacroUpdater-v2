package org.auriferous.macrodeob.hooks.record;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.auriferous.macrodeob.Main;
import org.auriferous.macrodeob.utils.InsnUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class HookRecord {
	public String hookName;
	public String owner;
	public String name;
	public String desc;
	public boolean isMethod;
	public boolean isStatic;
	
	public int paramCount;
	public Object modifier;
	
	public List<HookRecord> chain = new ArrayList<>();
	public boolean isSetter;
	
	public HookRecord() {
	}
	
	public HookRecord(String hook, String owner, String name, String desc, boolean isMethod, boolean isStatic, Object modifier) {
		this.hookName = hook;
		if (!isStatic)
			this.owner = getRealOwner(owner, name, desc);
		else 
			this.owner = owner;
		this.name = name;
		this.desc = desc;
		this.isMethod = isMethod;
		this.isStatic = isStatic;
		this.modifier = modifier;
	}
	
	public HookRecord(String hook, String owner, String name, String desc, boolean isMethod, boolean isStatic, boolean isSetter, Object modifier, int paramCount) {
		this(hook, owner, name, desc, isMethod, isStatic, modifier);
		this.paramCount = paramCount;
		this.isSetter = isSetter;
	}
	
	public void addLink(MethodInsnNode min) {
		boolean isStatic = min.getOpcode() == Opcodes.INVOKESTATIC;
		String owner = min.owner;
		if (!isStatic)
			owner = getRealOwner(min.owner, min.name, min.desc);
		addLink(new HookRecord(null, owner, min.name, min.desc, true, isStatic, getModifier(min)));
	}
	
	public void addLink(FieldInsnNode fin) {
		boolean isStatic = fin.getOpcode() == Opcodes.GETSTATIC;
		String owner = fin.owner;
		if (!isStatic)
			owner = getRealOwner(fin.owner, fin.name, fin.desc);
		addLink(new HookRecord(null, owner, fin.name, fin.desc, false, isStatic, getMultiplier(fin)));
	}
	
	public void addLink(HookRecord record) {
		chain.add(record);
	}
	
	private String getRealOwner(String owner, String name, String desc) {
		ClassNode cn = Main.rsClassLoader.loadClass(owner);
		if (cn == null)
			return null;
		
		for (FieldNode fn : cn.fields)
			if (fn.name.equals(name) && fn.desc.equals(desc))
				return cn.name;
		for (MethodNode mn : cn.methods)
			if (mn.name.equals(name) && mn.desc.equals(desc))
				return cn.name;
		
		String s = getRealOwner(cn.superName, name, desc);
		return s;
	}
	
	private Object getModifier(MethodInsnNode method) {
		if (method.getPrevious() instanceof LdcInsnNode && !(((LdcInsnNode)method.getPrevious()).cst instanceof String))
			return ((LdcInsnNode)method.getPrevious()).cst;
		return null;
	}

	private Object getMultiplier(FieldInsnNode field) {
		int sort = Type.getType(field.desc).getSort();
		Object multiplier = null;
		int op = field.getOpcode();
		boolean put = op == Opcodes.PUTFIELD || op == Opcodes.PUTSTATIC;
		if (InsnUtils.inRange(sort, Type.CHAR, Type.DOUBLE)) {
			AbstractInsnNode next = put ? field.getPrevious() : field.getNext();

			int opcode = next.getOpcode();
			if (InsnUtils.inRange(opcode, Opcodes.IMUL, Opcodes.DMUL)) {
				AbstractInsnNode prev = next;
				while ((prev = prev.getPrevious()) != null
						&& !(prev instanceof LdcInsnNode));
				if (prev instanceof LdcInsnNode)
					multiplier = ((LdcInsnNode) prev).cst;
			} else if (!put
					&& next instanceof LdcInsnNode
					&& InsnUtils.inRange(next.getNext().getOpcode(),
							Opcodes.IMUL, Opcodes.DMUL)) {
				multiplier = ((LdcInsnNode) next).cst;
			}
		}

		if (multiplier != null && put)
			multiplier = inverse(multiplier);

		return multiplier;
	}
	
	private static int inverse(Object input) {
		boolean isLong = input instanceof Long;
		BigInteger a = BigInteger.valueOf(Long.valueOf(input.toString()));
		BigInteger modulus = BigInteger.ONE.shiftLeft(isLong ? 64 : 32);
		return a.modInverse(modulus).intValue();
	}
}
