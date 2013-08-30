package org.auriferous.macrodeob.hooks;

import java.util.List;

import org.auriferous.macrodeob.hooks.record.ClassHook;
import org.auriferous.macrodeob.hooks.record.HooksMap;
import org.auriferous.macrodeob.transformers.base.TransformClassNode;
import org.auriferous.macrodeob.utils.InsnSearcher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class OptionsMenu extends Hook {

	private static int done = 0;
	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (mn.instructions.size() == 0)
				continue;
			if (isStatic(mn) && mn.desc.startsWith("(L")) {
				InsnSearcher finder = new InsnSearcher(mn);
				List<AbstractInsnNode[]> results = finder
						.search("iload[0-9]* bipush if_icmpeq iload[0-9]* sipush if_icmpne");
				boolean b = false;
				for (AbstractInsnNode[] result : results) {
					if (((IntInsnNode)result[1]).operand == 57 && ((IntInsnNode)result[4]).operand == 1007) {
						results = finder.search("getstatic");
						FieldInsnNode options = (FieldInsnNode) results.get(0)[0];
						HooksMap.CLIENT_HOOKS_MAP.addClientHook("Options", options);
						
						results = finder.search("getfield");
						FieldInsnNode action = (FieldInsnNode)results.get(3)[0];
						
						ClassHook option = HooksMap.CLIENT_HOOKS_MAP.addClassHook("OptionNode", action.owner);
						option.addFieldHook("Action", action);
						option.addFieldHook("Target", results.get(4)[0]);
						option.addFieldHook("OptionName", finder.searchSingle("getfield", result[1]));
						b = true;
						done++;
					}
				}
				if (b)
					continue;
			} else if (isStatic(mn) && mn.desc.startsWith("(II")) {
				if (containsGameString(mn, "Choose Option")) {
					InsnSearcher finder = new InsnSearcher(mn);
					List<AbstractInsnNode[]> results = finder.search("getstatic");
					HooksMap.CLIENT_HOOKS_MAP.addClientHook("OptionsCount", results.get(1)[0]);
					
					results = finder.search("getfield");
					FieldInsnNode optionType = (FieldInsnNode)results.get(1)[0];
					
					ClassHook option = HooksMap.CLIENT_HOOKS_MAP.addClassHook("OptionNode", optionType.owner);
					option.addFieldHook("Option", optionType);
					
					results = finder.search("putstatic");
					
					HooksMap.CLIENT_HOOKS_MAP.addClientHook("OptionsBoxHeight", results.get(1)[0]);
					HooksMap.CLIENT_HOOKS_MAP.addClientHook("OptionsBoxX", results.get(2)[0]);
					HooksMap.CLIENT_HOOKS_MAP.addClientHook("OptionsBoxY", results.get(3)[0]);
					HooksMap.CLIENT_HOOKS_MAP.addClientHook("OptionsBoxWidth", results.get(4)[0]);
					HooksMap.CLIENT_HOOKS_MAP.addClientHook("OptionsBoxOffset", results.get(5)[0]);
					HooksMap.CLIENT_HOOKS_MAP.addClientHook("OptionsBoxOpen", results.get(6)[0]);
					
					done++;
				}
			}
			if (done == 2)
				return true;
		}
		return false;
	}

}
