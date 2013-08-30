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
import org.objectweb.asm.tree.MethodNode;

public class FriendIgnoreFields extends Hook {

	@Override
	public boolean accept(TransformClassNode tcn) {
		for (MethodNode mn : tcn.methods) {
			if (isStatic(mn) && mn.desc.startsWith("(L") && containsGameString(mn, "Walk here")) {
				InsnSearcher finder = new InsnSearcher(mn);
				List<AbstractInsnNode[]> results = finder.search(".ipush if_icmpge new");
				boolean done = false;
				for (AbstractInsnNode[] result : results) {
					if (((IntInsnNode)result[0]).operand == 200) {
						ClassHook client = HooksMap.CLIENT_HOOKS_MAP.addClassHook("Client", "client");
						FieldInsnNode fin = (FieldInsnNode)finder.searchBackwardSingle("getstatic", result[0]);
						client.addStaticFieldHook("FriendsCount", fin);
						
						fin = (FieldInsnNode)finder.searchSingle("getstatic", result[0]);
						client.addStaticFieldHook("Friends", fin);
						
						List<AbstractInsnNode[]> putFields = finder.search("putfield", result[0]);
						fin = (FieldInsnNode)putFields.get(0)[0];
						
						ClassHook friend = HooksMap.CLIENT_HOOKS_MAP.addClassHook("Friend", fin);
						friend.addFieldHook("Name", fin);
						friend.addFieldHook("World", (FieldInsnNode)putFields.get(2)[0]);
						friend.addFieldHook("WorldString", (FieldInsnNode)putFields.get(3)[0]);
						done = true;
					} else if (((IntInsnNode)result[0]).operand == 100) {

						ClassHook client = HooksMap.CLIENT_HOOKS_MAP.addClassHook("Client", "client");
						FieldInsnNode fin = (FieldInsnNode)finder.searchBackwardSingle("getstatic", result[0]);
						client.addStaticFieldHook("IgnoredCount", fin);
						
						fin = (FieldInsnNode)finder.searchSingle("getstatic", result[0]);
						client.addStaticFieldHook("IgnoredPlayers", fin);
						
						List<AbstractInsnNode[]> putFields = finder.search("putfield", result[0]);
						fin = (FieldInsnNode)putFields.get(0)[0];
						
						ClassHook friend = HooksMap.CLIENT_HOOKS_MAP.addClassHook("IgnoredPlayer", fin);
						friend.addFieldHook("Name", fin);
					}
				}
				if (done)
					return true;
			}
		}
		return false;
	}
	
}
