package org.auriferous.macrodeob.hooks.record;

import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.HashMap;

import org.auriferous.macrodeob.utils.InsnUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.Opcodes;

public class ClassHook {
	public String hookName;
	public String name;
	public HashMap<String, HookRecord> hooks = new HashMap<>();

	public ClassHook(String hookName, String name) {
		this.hookName = hookName;
		this.name = name;
	}

	public HookRecord addFieldHook(String hookName, String owner, String name, String desc,
			Object multiplier) {
		return addHook(hookName, owner, name, desc, false, false, false, multiplier, 0);
	}

	public HookRecord addFieldHook(String hookName, FieldInsnNode field) {
		return addFieldHook(hookName, field.owner, field.name, field.desc, getMultiplier(field));
	}
	
	public HookRecord addFieldHook(String hookName, AbstractInsnNode in) {
		FieldInsnNode field = (FieldInsnNode)in;
		return addFieldHook(hookName, field.owner, field.name, field.desc, getMultiplier(field));
	}

	public HookRecord addFieldHook(String hookName, FieldNode field, Object multiplier) {
		return addFieldHook(hookName, name, field.name, field.desc, multiplier);
	}
	
	public HookRecord addStaticFieldHook(String hookName, String owner, String name,
			String desc, boolean isSetter, Object multiplier) {
		return addHook(hookName, owner, name, desc, false, true, isSetter, multiplier, 0);
	}

	public HookRecord addStaticFieldHook(String hookName, String owner, String name,
			String desc, Object multiplier) {
		return addHook(hookName, owner, name, desc, false, true, false, multiplier, 0);
	}
	
	public HookRecord addStaticFieldSetterHook(String hookName, FieldInsnNode field) {
		return addStaticFieldHook(hookName, field.owner, field.name, field.desc,
				true, getMultiplier(field));
	}

	public HookRecord addStaticFieldHook(String hookName, FieldInsnNode field) {
		return addStaticFieldHook(hookName, field.owner, field.name, field.desc,
				getMultiplier(field));
	}
	
	public HookRecord addMethodHook(String hookName, MethodInsnNode method) {
		return addMethodHook(hookName, method, 0);
	}
	
	public HookRecord addMethodHook(String hookName, String owner, MethodNode method, boolean isSetter, int paramCount) {
		return addHook(hookName, owner, method.name, method.desc, true, Modifier.isStatic(method.access), isSetter, null, paramCount);
	}
	
	public HookRecord addMethodHook(String hookName, MethodInsnNode method, int paramCount) {
		if (paramCount == Type.getArgumentTypes(method.desc).length)
			return addHook(hookName, method.owner, method.name, method.desc, true, method.getOpcode() == Opcodes.INVOKESTATIC, false, null, paramCount);
		else {
			return addHook(hookName, method.owner, method.name, method.desc, true, method.getOpcode() == Opcodes.INVOKESTATIC, false, getModifier(method), paramCount);
		}
	}

	private HookRecord addHook(String hookName, String owner, String name,
			String desc, boolean isMethod, boolean isStatic, boolean isSetter, Object modifier, int paramCount) {
		
		HookRecord record = new HookRecord(hookName, owner, name, desc,
				isMethod, isStatic, isSetter, modifier, paramCount);
		hooks.put(hookName, record);
		return record;
	}

	public boolean containsFieldHook(String hookName) {
		return hooks.containsKey(hookName);
	}
	
	private Object getModifier(MethodInsnNode method) {
		AbstractInsnNode prev = method.getPrevious();
		if (isNumber(prev))
			return getNumber(prev);
		return null;
	}
	
	private boolean isNumber(AbstractInsnNode in) {
		if (in instanceof LdcInsnNode && !(((LdcInsnNode)in).cst instanceof String))
			return true;
		else if (in instanceof IntInsnNode)
			return true;
		else if (InsnUtils.inRange(in.getOpcode(), Opcodes.ICONST_M1, Opcodes.ICONST_5))
			return true;
		return false;
	}
	
	private Object getNumber(AbstractInsnNode in) {
		if (in instanceof LdcInsnNode && !(((LdcInsnNode)in).cst instanceof String))
			return ((LdcInsnNode)in).cst;
		else if (in instanceof IntInsnNode)
			return ((IntInsnNode)in).operand;
		else if (InsnUtils.inRange(in.getOpcode(), Opcodes.ICONST_M1, Opcodes.ICONST_5))
			return in.getOpcode()-3;
		return false;
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
				if (prev instanceof LdcInsnNode) {
					multiplier = ((LdcInsnNode) prev).cst;
					
				}
			} else if (!put
					&& next instanceof LdcInsnNode
					&& InsnUtils.inRange(next.getNext().getOpcode(),
							Opcodes.IMUL, Opcodes.DMUL)) {
				multiplier = ((LdcInsnNode) next).cst;
			}
		}

		if (multiplier != null && put) {
			multiplier = inverse(multiplier);
		}

		return multiplier;
	}

	public static int inverse(Object input) {
		boolean isLong = input instanceof Long;
		BigInteger a = BigInteger.valueOf(Long.valueOf(input.toString()));
		BigInteger modulus = BigInteger.ONE.shiftLeft(isLong ? 64 : 32);
		return a.modInverse(modulus).intValue();
	}
}
