package dev.jorel.commandapi.nms;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.game.ClientboundChatPreviewPacket;
import net.minecraft.network.protocol.game.ServerboundChatPreviewPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class NMS_1_19_R1_ChatPreviewHandler extends NMS_1_19_Common_ChatPreviewHandler {

	public NMS_1_19_R1_ChatPreviewHandler(NMS<CommandSourceStack> nms, Plugin plugin, Player player) {
		super(nms, plugin, player);
	}

	@Override
	protected void handleChatPreviewPacket(ServerboundChatPreviewPacket chatPreview) {
		// We want to run this synchronously, just in case there's some funky async stuff going on here
		Bukkit.getScheduler().runTask(this.plugin, () -> this.connection.send(
			new ClientboundChatPreviewPacket(chatPreview.queryId(), parseChatPreviewQuery(chatPreview.query()))
		));
	}
}