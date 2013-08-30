package org.auriferous.macrodeob.transformers.miners;

import java.util.HashMap;
import java.util.List;

import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.transformers.methods.MethodTransform;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.auriferous.macrodeob.utils.InsnUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class GameString implements MethodTransform {

	public static HashMap<String, String> stringMap = new HashMap<>();
	public static HashMap<String, String> intMap = new HashMap<>();

	@Override
	public boolean accept(ClassNode tcn, MethodNode mn) {
		if (mn.name.equals("<clinit>")) {
			InsnSearcher finder = new InsnSearcher(mn);
			List<AbstractInsnNode[]> results = finder
					.search("ldc ldc ldc ldc ldc invokespecial putstatic");
			for (AbstractInsnNode[] result : results) {
				FieldInsnNode fin = (FieldInsnNode) result[6];
				stringMap.put(fin.owner + "." + fin.name,
						((LdcInsnNode) result[0]).cst.toString());
			}

			String intInsn = "(.ipush|iconst_.|iconst_m1)";
			results = finder.search("dup " + intInsn + " " + intInsn
					+ " invokespecial putstatic");
			if (results.size() > 160) {
				for (AbstractInsnNode[] result : results) {
					FieldInsnNode fin = (FieldInsnNode) result[4];
					intMap.put(fin.owner + "." + fin.name, ""+getInt(result[1]));
				}
			}
		}
		return false;
	}

	private int getInt(AbstractInsnNode in) {
		if (InsnUtils.inRange(in.getOpcode(), Opcodes.ICONST_M1,
				Opcodes.ICONST_5)) {
			return in.getOpcode() - 3;
		} else
			return ((IntInsnNode) in).operand;
	}

}
