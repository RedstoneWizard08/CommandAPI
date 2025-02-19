package dev.jorel.commandapi.nms;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import com.mojang.brigadier.Message;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_16_R2.CraftLootTable;
import org.bukkit.craftbukkit.v1_16_R2.CraftParticle;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.craftbukkit.v1_16_R2.CraftSound;
import org.bukkit.craftbukkit.v1_16_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_16_R2.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.v1_16_R2.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.help.CustomHelpTopic;
import org.bukkit.craftbukkit.v1_16_R2.help.SimpleHelpMap;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R2.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v1_16_R2.util.CraftChatMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.potion.PotionEffectType;

import com.google.common.io.Files;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.arguments.SuggestionProviders;
import dev.jorel.commandapi.preprocessor.Differs;
import dev.jorel.commandapi.preprocessor.NMSMeta;
import dev.jorel.commandapi.preprocessor.RequireField;
import dev.jorel.commandapi.wrappers.ComplexRecipeImpl;
import dev.jorel.commandapi.wrappers.FloatRange;
import dev.jorel.commandapi.wrappers.FunctionWrapper;
import dev.jorel.commandapi.wrappers.IntegerRange;
import dev.jorel.commandapi.wrappers.Location2D;
import dev.jorel.commandapi.wrappers.MathOperation;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import dev.jorel.commandapi.wrappers.ParticleData;
import dev.jorel.commandapi.wrappers.Rotation;
import dev.jorel.commandapi.wrappers.ScoreboardSlot;
import dev.jorel.commandapi.wrappers.SimpleFunctionWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_16_R2.Advancement;
import net.minecraft.server.v1_16_R2.ArgumentAngle;
import net.minecraft.server.v1_16_R2.ArgumentBlockPredicate;
import net.minecraft.server.v1_16_R2.ArgumentChat;
import net.minecraft.server.v1_16_R2.ArgumentChatComponent;
import net.minecraft.server.v1_16_R2.ArgumentChatFormat;
import net.minecraft.server.v1_16_R2.ArgumentCriterionValue;
import net.minecraft.server.v1_16_R2.ArgumentDimension;
import net.minecraft.server.v1_16_R2.ArgumentEnchantment;
import net.minecraft.server.v1_16_R2.ArgumentEntity;
import net.minecraft.server.v1_16_R2.ArgumentEntitySummon;
import net.minecraft.server.v1_16_R2.ArgumentItemPredicate;
import net.minecraft.server.v1_16_R2.ArgumentItemStack;
import net.minecraft.server.v1_16_R2.ArgumentMathOperation;
import net.minecraft.server.v1_16_R2.ArgumentMinecraftKeyRegistered;
import net.minecraft.server.v1_16_R2.ArgumentMobEffect;
import net.minecraft.server.v1_16_R2.ArgumentNBTTag;
import net.minecraft.server.v1_16_R2.ArgumentParticle;
import net.minecraft.server.v1_16_R2.ArgumentPosition;
import net.minecraft.server.v1_16_R2.ArgumentProfile;
import net.minecraft.server.v1_16_R2.ArgumentRegistry;
import net.minecraft.server.v1_16_R2.ArgumentRotation;
import net.minecraft.server.v1_16_R2.ArgumentRotationAxis;
import net.minecraft.server.v1_16_R2.ArgumentScoreboardCriteria;
import net.minecraft.server.v1_16_R2.ArgumentScoreboardObjective;
import net.minecraft.server.v1_16_R2.ArgumentScoreboardSlot;
import net.minecraft.server.v1_16_R2.ArgumentScoreboardTeam;
import net.minecraft.server.v1_16_R2.ArgumentScoreholder;
import net.minecraft.server.v1_16_R2.ArgumentTag;
import net.minecraft.server.v1_16_R2.ArgumentTile;
import net.minecraft.server.v1_16_R2.ArgumentTime;
import net.minecraft.server.v1_16_R2.ArgumentUUID;
import net.minecraft.server.v1_16_R2.ArgumentVec2;
import net.minecraft.server.v1_16_R2.ArgumentVec2I;
import net.minecraft.server.v1_16_R2.ArgumentVec3;
import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.BlockPosition2D;
import net.minecraft.server.v1_16_R2.CommandListenerWrapper;
import net.minecraft.server.v1_16_R2.CompletionProviders;
import net.minecraft.server.v1_16_R2.CriterionConditionValue;
import net.minecraft.server.v1_16_R2.CustomFunction;
import net.minecraft.server.v1_16_R2.CustomFunctionData;
import net.minecraft.server.v1_16_R2.CustomFunctionManager;
import net.minecraft.server.v1_16_R2.DataPackResources;
import net.minecraft.server.v1_16_R2.Entity;
import net.minecraft.server.v1_16_R2.EntityPlayer;
import net.minecraft.server.v1_16_R2.EntitySelector;
import net.minecraft.server.v1_16_R2.EntityTypes;
import net.minecraft.server.v1_16_R2.EnumDirection.EnumAxis;
import net.minecraft.server.v1_16_R2.IBlockData;
import net.minecraft.server.v1_16_R2.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_16_R2.ICompletionProvider;
import net.minecraft.server.v1_16_R2.IRecipe;
import net.minecraft.server.v1_16_R2.IRegistry;
import net.minecraft.server.v1_16_R2.IReloadableResourceManager;
import net.minecraft.server.v1_16_R2.ItemStack;
import net.minecraft.server.v1_16_R2.MinecraftKey;
import net.minecraft.server.v1_16_R2.MinecraftServer;
import net.minecraft.server.v1_16_R2.ParticleParam;
import net.minecraft.server.v1_16_R2.ParticleParamBlock;
import net.minecraft.server.v1_16_R2.ParticleParamItem;
import net.minecraft.server.v1_16_R2.ParticleParamRedstone;
import net.minecraft.server.v1_16_R2.ShapeDetectorBlock;
import net.minecraft.server.v1_16_R2.SystemUtils;
import net.minecraft.server.v1_16_R2.Unit;
import net.minecraft.server.v1_16_R2.Vec2F;
import net.minecraft.server.v1_16_R2.Vec3D;

/**
 * NMS implementation for Minecraft 1.16.2 and 1.16.3
 */
@NMSMeta(compatibleWith = { "1.16.2", "1.16.3" })
@RequireField(in = DataPackResources.class, name = "b", ofType = IReloadableResourceManager.class)
@RequireField(in = CustomFunctionManager.class, name = "h", ofType = CommandDispatcher.class)
@RequireField(in = CraftSound.class, name = "minecraftKey", ofType = String.class)
@RequireField(in = EntitySelector.class, name = "checkPermissions", ofType = boolean.class)
@RequireField(in = SimpleHelpMap.class, name = "helpTopics", ofType = Map.class)
@RequireField(in = ParticleParamBlock.class, name = "c", ofType = IBlockData.class)
@RequireField(in = ParticleParamItem.class, name = "c", ofType = ItemStack.class)
@RequireField(in = ParticleParamRedstone.class, name = "f", ofType = float.class)
public class NMS_1_16_R2 extends NMSWrapper_1_16_R2 {

	private static final MinecraftServer MINECRAFT_SERVER = ((CraftServer) Bukkit.getServer()).getServer();
	private static final VarHandle DataPackResources_b;
	private static final VarHandle SimpleHelpMap_helpTopics;
	private static final VarHandle ParticleParamBlock_c;
	private static final VarHandle ParticleParamItem_c;
	private static final VarHandle ParticleParamRedstone_f;

	// Compute all var handles all in one go so we don't do this during main server
	// runtime
	static {
		VarHandle dpr_b = null;
		VarHandle shm_ht = null;
		VarHandle ppb_c = null;
		VarHandle ppi_c = null;
		VarHandle ppr_g = null;
		try {
			dpr_b = MethodHandles.privateLookupIn(DataPackResources.class, MethodHandles.lookup())
					.findVarHandle(DataPackResources.class, "b", IReloadableResourceManager.class);
			shm_ht = MethodHandles.privateLookupIn(SimpleHelpMap.class, MethodHandles.lookup())
					.findVarHandle(SimpleHelpMap.class, "helpTopics", Map.class);
			ppb_c = MethodHandles.privateLookupIn(ParticleParamBlock.class, MethodHandles.lookup())
					.findVarHandle(ParticleParamBlock.class, "c", IBlockData.class);
			ppb_c = MethodHandles.privateLookupIn(ParticleParamItem.class, MethodHandles.lookup())
					.findVarHandle(ParticleParamItem.class, "c", ItemStack.class);
			ppr_g = MethodHandles.privateLookupIn(ParticleParamRedstone.class, MethodHandles.lookup())
					.findVarHandle(ParticleParamRedstone.class, "f", float.class);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		DataPackResources_b = dpr_b;
		SimpleHelpMap_helpTopics = shm_ht;
		ParticleParamBlock_c = ppb_c;
		ParticleParamItem_c = ppi_c;
		ParticleParamRedstone_f = ppr_g;
	}

	@SuppressWarnings("deprecation")
	private static NamespacedKey fromMinecraftKey(MinecraftKey key) {
		return new NamespacedKey(key.getNamespace(), key.getKey());
	}

	@Differs(from = "1.16.1", by = "Adds AngleArgument")
	@Override
	public ArgumentType<?> _ArgumentAngle() {
		return ArgumentAngle.a();
	}

	@Override
	public ArgumentType<?> _ArgumentAxis() {
		return ArgumentRotationAxis.a();
	}

	@Override
	public ArgumentType<?> _ArgumentBlockPredicate() {
		return ArgumentBlockPredicate.a();
	}

	@Override
	public ArgumentType<?> _ArgumentBlockState() {
		return ArgumentTile.a();
	}

	@Override
	public ArgumentType<?> _ArgumentChat() {
		return ArgumentChat.a();
	}

	@Override
	public ArgumentType<?> _ArgumentChatComponent() {
		return ArgumentChatComponent.a();
	}

	@Override
	public ArgumentType<?> _ArgumentChatFormat() {
		return ArgumentChatFormat.a();
	}

	@Override
	public ArgumentType<?> _ArgumentDimension() {
		return ArgumentDimension.a();
	}

	@Override
	public ArgumentType<?> _ArgumentEnchantment() {
		return ArgumentEnchantment.a();
	}

	@Override
	public ArgumentType<?> _ArgumentEntity(
			dev.jorel.commandapi.arguments.EntitySelector selector) {
		return switch (selector) {
			case MANY_ENTITIES -> ArgumentEntity.multipleEntities();
			case MANY_PLAYERS -> ArgumentEntity.d();
			case ONE_ENTITY -> ArgumentEntity.a();
			case ONE_PLAYER -> ArgumentEntity.c();
		};
	}

	@Override
	public ArgumentType<?> _ArgumentEntitySummon() {
		return ArgumentEntitySummon.a();
	}

	@Override
	public ArgumentType<?> _ArgumentFloatRange() {
		return new ArgumentCriterionValue.a();
	}

	@Override
	public ArgumentType<?> _ArgumentIntRange() {
		return new ArgumentCriterionValue.b();
	}

	@Override
	public ArgumentType<?> _ArgumentItemPredicate() {
		return ArgumentItemPredicate.a();
	}

	@Override
	public ArgumentType<?> _ArgumentItemStack() {
		return ArgumentItemStack.a();
	}

	@Override
	public ArgumentType<?> _ArgumentMathOperation() {
		return ArgumentMathOperation.a();
	}

	@Override
	public ArgumentType<?> _ArgumentMinecraftKeyRegistered() {
		return ArgumentMinecraftKeyRegistered.a();
	}

	@Override
	public ArgumentType<?> _ArgumentMobEffect() {
		return ArgumentMobEffect.a();
	}

	@Override
	public ArgumentType<?> _ArgumentNBTCompound() {
		return ArgumentNBTTag.a();
	}

	@Override
	public ArgumentType<?> _ArgumentParticle() {
		return ArgumentParticle.a();
	}

	@Override
	public ArgumentType<?> _ArgumentPosition() {
		return ArgumentPosition.a();
	}

	@Override
	public ArgumentType<?> _ArgumentPosition2D() {
		return ArgumentVec2I.a();
	}

	@Override
	public ArgumentType<?> _ArgumentProfile() {
		return ArgumentProfile.a();
	}

	@Override
	public ArgumentType<?> _ArgumentRotation() {
		return ArgumentRotation.a();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardCriteria() {
		return ArgumentScoreboardCriteria.a();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardObjective() {
		return ArgumentScoreboardObjective.a();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardSlot() {
		return ArgumentScoreboardSlot.a();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardTeam() {
		return ArgumentScoreboardTeam.a();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreholder(boolean single) {
		return single ? ArgumentScoreholder.a() : ArgumentScoreholder.b();
	}

	@Override
	public ArgumentType<?> _ArgumentSyntheticBiome() {
		return _ArgumentMinecraftKeyRegistered();
	}

	@Override
	public ArgumentType<?> _ArgumentTag() {
		return ArgumentTag.a();
	}

	@Override
	public ArgumentType<?> _ArgumentTime() {
		return ArgumentTime.a();
	}

	@Override
	public ArgumentType<?> _ArgumentUUID() {
		return ArgumentUUID.a();
	}

	@Override
	public ArgumentType<?> _ArgumentVec2() {
		return ArgumentVec2.a();
	}

	@Override
	public ArgumentType<?> _ArgumentVec3() {
		return ArgumentVec3.a();
	}

	@Override
	public void addToHelpMap(Map<String, HelpTopic> helpTopicsToAdd) {
		Map<String, HelpTopic> helpTopics = (Map<String, HelpTopic>) SimpleHelpMap_helpTopics
				.get(Bukkit.getServer().getHelpMap());
		for (Map.Entry<String, HelpTopic> entry : helpTopicsToAdd.entrySet()) {
			helpTopics.put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public String[] compatibleVersions() {
		return new String[] { "1.16.2", "1.16.3" };
	}

	@Override
	public String convert(org.bukkit.inventory.ItemStack is) {
		return is.getType().getKey().toString() + CraftItemStack.asNMSCopy(is).getOrCreateTag().toString();
	}

	@Override
	public String convert(ParticleData<?> particle) {
		return CraftParticle.toNMS(particle.particle(), particle.data()).a();
	}

	@Override
	public String convert(PotionEffectType potion) {
		return potion.getName().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public String convert(Sound sound) {
		return CraftSound.getSound(sound);
	}

	// Converts NMS function to SimpleFunctionWrapper
	private SimpleFunctionWrapper convertFunction(CustomFunction customFunction) {
		ToIntFunction<CommandListenerWrapper> appliedObj = clw -> MINECRAFT_SERVER.getFunctionData().a(customFunction,
				clw);

		Object[] cArr = customFunction.b();
		String[] result = new String[cArr.length];
		for (int i = 0, size = cArr.length; i < size; i++) {
			result[i] = cArr[i].toString();
		}
		return new SimpleFunctionWrapper(fromMinecraftKey(customFunction.a()), appliedObj, result);
	}

	@Override
	public void createDispatcherFile(File file,
			com.mojang.brigadier.CommandDispatcher<CommandListenerWrapper> dispatcher) throws IOException {
		Files.write(new GsonBuilder().setPrettyPrinting().create()
				.toJson(ArgumentRegistry.a(dispatcher, dispatcher.getRoot())), file, StandardCharsets.UTF_8);
	}

	@Override
	public HelpTopic generateHelpTopic(String commandName, String shortDescription, String fullDescription,
			String permission) {
		return new CustomHelpTopic(commandName, shortDescription, fullDescription, permission);
	}

	@Override
	public org.bukkit.advancement.Advancement getAdvancement(CommandContext<CommandListenerWrapper> cmdCtx, String key)
			throws CommandSyntaxException {
		return ArgumentMinecraftKeyRegistered.a(cmdCtx, key).bukkit;
	}

	@Override
	public Component getAdventureChat(CommandContext<CommandListenerWrapper> cmdCtx, String key)
			throws CommandSyntaxException {
		String jsonString = ChatSerializer.a(ArgumentChat.a(cmdCtx, key));
		return GsonComponentSerializer.gson().deserialize(jsonString);
	}

	@Override
	public Component getAdventureChatComponent(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		String jsonString = ChatSerializer.a(ArgumentChatComponent.a(cmdCtx, key));
		return GsonComponentSerializer.gson().deserialize(jsonString);
	}

	@Differs(from = "1.16.1", by = "Implements AngleArgument")
	@Override
	public float getAngle(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		return ArgumentAngle.a(cmdCtx, key);
	}

	@Override
	public EnumSet<Axis> getAxis(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		EnumSet<Axis> set = EnumSet.noneOf(Axis.class);
		EnumSet<EnumAxis> parsedEnumSet = ArgumentRotationAxis.a(cmdCtx, key);
		for (EnumAxis element : parsedEnumSet) {
			set.add(switch (element) {
				case X -> Axis.X;
				case Y -> Axis.Y;
				case Z -> Axis.Z;
			});
		}
		return set;
	}

	@Override
	public Biome getBiome(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		return Biome.valueOf(cmdCtx.getArgument(key, MinecraftKey.class).getKey().toUpperCase());
	}

	@Override
	public Predicate<Block> getBlockPredicate(CommandContext<CommandListenerWrapper> cmdCtx, String key)
			throws CommandSyntaxException {
		Predicate<ShapeDetectorBlock> predicate = ArgumentBlockPredicate.a(cmdCtx, key);
		return (Block block) -> {
			return predicate.test(new ShapeDetectorBlock(cmdCtx.getSource().getWorld(),
					new BlockPosition(block.getX(), block.getY(), block.getZ()), true));
		};
	}

	@Override
	public BlockData getBlockState(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		return CraftBlockData.fromData(ArgumentTile.a(cmdCtx, key).a());
	}

	@Override
	public com.mojang.brigadier.CommandDispatcher<CommandListenerWrapper> getBrigadierDispatcher() {
		return MINECRAFT_SERVER.vanillaCommandDispatcher.a();
	}

	@Override
	public BaseComponent[] getChat(CommandContext<CommandListenerWrapper> cmdCtx, String key)
			throws CommandSyntaxException {
		return ComponentSerializer.parse(ChatSerializer.a(ArgumentChat.a(cmdCtx, key)));
	}

	@Override
	public ChatColor getChatColor(CommandContext<CommandListenerWrapper> cmdCtx, String str) {
		return CraftChatMessage.getColor(ArgumentChatFormat.a(cmdCtx, str));
	}

	@Override
	public BaseComponent[] getChatComponent(CommandContext<CommandListenerWrapper> cmdCtx, String str) {
		return ComponentSerializer.parse(ChatSerializer.a(ArgumentChatComponent.a(cmdCtx, str)));
	}

	@Override
	public CommandListenerWrapper getCLWFromCommandSender(CommandSender sender) {
		return VanillaCommandWrapper.getListener(sender);
	}

	@Override
	public CommandSender getCommandSenderFromCSS(CommandListenerWrapper clw) {
		try {
			return clw.getBukkitSender();
		} catch (UnsupportedOperationException e) {
			return null;
		}
	}

	@Override
	public Environment getDimension(CommandContext<CommandListenerWrapper> cmdCtx, String key)
			throws CommandSyntaxException {
		return ArgumentDimension.a(cmdCtx, key).getWorld().getEnvironment();
	}

	@Override
	public Enchantment getEnchantment(CommandContext<CommandListenerWrapper> cmdCtx, String str) {
		return new CraftEnchantment(ArgumentEnchantment.a(cmdCtx, str));
	}

	@Override
	public Object getEntitySelector(CommandContext<CommandListenerWrapper> cmdCtx, String str,
			dev.jorel.commandapi.arguments.EntitySelector selector)
			throws CommandSyntaxException {
		EntitySelector argument = cmdCtx.getArgument(str, EntitySelector.class);
		try {
			CommandAPIHandler.getInstance().getField(EntitySelector.class, "checkPermissions").set(argument, false);
		} catch (IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}

		return switch (selector) {
			case MANY_ENTITIES:
				// ArgumentEntity.c -> EntitySelector.getEntities
				try {
					List<org.bukkit.entity.Entity> result = new ArrayList<>();
					for (Entity entity : argument.getEntities(cmdCtx.getSource())) {
						result.add(entity.getBukkitEntity());
					}
					yield result;
				} catch (CommandSyntaxException e) {
					yield new ArrayList<org.bukkit.entity.Entity>();
				}
			case MANY_PLAYERS:
				// ArgumentEntity.d -> EntitySelector.d
				try {
					List<Player> result = new ArrayList<>();
					for (EntityPlayer player : argument.d(cmdCtx.getSource())) {
						result.add(player.getBukkitEntity());
					}
					yield result;
				} catch (CommandSyntaxException e) {
					yield new ArrayList<Player>();
				}
			case ONE_ENTITY:
				// ArgumentEntity.a -> EntitySelector.a
				yield argument.a(cmdCtx.getSource()).getBukkitEntity();
			case ONE_PLAYER:
				// ArgumentEntity.e -> EntitySelector.c
				yield argument.c(cmdCtx.getSource()).getBukkitEntity();
		};
	}

	@Differs(from = "1.16.1", by = "Uses deprecated EntityType.fromName instead of IRegistry.ENTITY_TYPE")
	@SuppressWarnings("deprecation")
	@Override
	public EntityType getEntityType(CommandContext<CommandListenerWrapper> cmdCtx, String str)
			throws CommandSyntaxException {
		return EntityType
				.fromName(EntityTypes.getName(IRegistry.ENTITY_TYPE.get(ArgumentEntitySummon.a(cmdCtx, str))).getKey());
	}

	@Override
	public FloatRange getFloatRange(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		CriterionConditionValue.FloatRange range = cmdCtx.getArgument(key,
				CriterionConditionValue.FloatRange.class);
		float low = range.a() == null ? -Float.MAX_VALUE : range.a();
		float high = range.b() == null ? Float.MAX_VALUE : range.b();
		return new FloatRange(low, high);
	}

	@Override
	public FunctionWrapper[] getFunction(CommandContext<CommandListenerWrapper> cmdCtx, String str)
			throws CommandSyntaxException {
		List<FunctionWrapper> result = new ArrayList<>();
		CommandListenerWrapper commandListenerWrapper = cmdCtx.getSource().a().b(2);

		for (CustomFunction customFunction : ArgumentTag.a(cmdCtx, str)) {
			result.add(FunctionWrapper.fromSimpleFunctionWrapper(convertFunction(customFunction),
					commandListenerWrapper, e -> {
						return cmdCtx.getSource().a(((CraftEntity) e).getHandle());
					}));
		}

		return result.toArray(new FunctionWrapper[0]);
	}

	@Override
	public SimpleFunctionWrapper getFunction(NamespacedKey key) {
		return convertFunction(
				MINECRAFT_SERVER.getFunctionData().a(new MinecraftKey(key.getNamespace(), key.getKey())).get());
	}

	@Override
	public Set<NamespacedKey> getFunctions() {
		Set<NamespacedKey> functions = new HashSet<>();
		for (MinecraftKey key : MINECRAFT_SERVER.getFunctionData().f()) {
			functions.add(fromMinecraftKey(key));
		}
		return functions;
	}

	@Override
	public IntegerRange getIntRange(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		CriterionConditionValue.IntegerRange range = ArgumentCriterionValue.b.a(cmdCtx, key);
		int low = range.a() == null ? Integer.MIN_VALUE : range.a();
		int high = range.b() == null ? Integer.MAX_VALUE : range.b();
		return new IntegerRange(low, high);
	}

	@Override
	public org.bukkit.inventory.ItemStack getItemStack(CommandContext<CommandListenerWrapper> cmdCtx, String str)
			throws CommandSyntaxException {
		return CraftItemStack.asBukkitCopy(ArgumentItemStack.a(cmdCtx, str).a(1, false));
	}

	@Override
	public Predicate<org.bukkit.inventory.ItemStack> getItemStackPredicate(
			CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		Predicate<ItemStack> predicate = ArgumentItemPredicate.a(cmdCtx, key);
		return item -> predicate.test(CraftItemStack.asNMSCopy(item));
	}

	@Override
	public Location2D getLocation2DBlock(CommandContext<CommandListenerWrapper> cmdCtx, String key)
			throws CommandSyntaxException {
		BlockPosition2D blockPos = ArgumentVec2I.a(cmdCtx, key);
		return new Location2D(getWorldForCSS(cmdCtx.getSource()), blockPos.a, blockPos.b);
	}

	@Override
	public Location2D getLocation2DPrecise(CommandContext<CommandListenerWrapper> cmdCtx, String key)
			throws CommandSyntaxException {
		Vec2F vecPos = ArgumentVec2.a(cmdCtx, key);
		return new Location2D(getWorldForCSS(cmdCtx.getSource()), vecPos.i, vecPos.j);
	}

	@Override
	public Location getLocationBlock(CommandContext<CommandListenerWrapper> cmdCtx, String str)
			throws CommandSyntaxException {
		BlockPosition blockPos = ArgumentPosition.a(cmdCtx, str);
		return new Location(getWorldForCSS(cmdCtx.getSource()), blockPos.getX(), blockPos.getY(), blockPos.getZ());
	}

	@Override
	public Location getLocationPrecise(CommandContext<CommandListenerWrapper> cmdCtx, String str)
			throws CommandSyntaxException {
		Vec3D vecPos = ArgumentVec3.a(cmdCtx, str);
		return new Location(getWorldForCSS(cmdCtx.getSource()), vecPos.x, vecPos.y, vecPos.z);
	}

	@Override
	public org.bukkit.loot.LootTable getLootTable(CommandContext<CommandListenerWrapper> cmdCtx, String str) {
		MinecraftKey minecraftKey = ArgumentMinecraftKeyRegistered.e(cmdCtx, str);
		return new CraftLootTable(fromMinecraftKey(minecraftKey),
				MINECRAFT_SERVER.getLootTableRegistry().getLootTable(minecraftKey));
	}

	@Override
	public MathOperation getMathOperation(CommandContext<CommandListenerWrapper> cmdCtx, String key)
			throws CommandSyntaxException {
		// We run this to ensure the argument exists/parses properly
		ArgumentMathOperation.a(cmdCtx, key);
		return MathOperation.fromString(CommandAPIHandler.getRawArgumentInput(cmdCtx, key));
	}

	@SuppressWarnings("deprecation")
	@Override
	public NamespacedKey getMinecraftKey(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		MinecraftKey resourceLocation = ArgumentMinecraftKeyRegistered.e(cmdCtx, key);
		return new NamespacedKey(resourceLocation.getNamespace(), resourceLocation.getKey());
	}

	@Override
	public <NBTContainer> Object getNBTCompound(CommandContext<CommandListenerWrapper> cmdCtx, String key,
			Function<Object, NBTContainer> nbtContainerConstructor) {
		return nbtContainerConstructor.apply(ArgumentNBTTag.a(cmdCtx, key));
	}

	@Override
	public String getObjective(CommandContext<CommandListenerWrapper> cmdCtx, String key)
			throws IllegalArgumentException, CommandSyntaxException {
		return ArgumentScoreboardObjective.a(cmdCtx, key).getName();
	}

	@Override
	public String getObjectiveCriteria(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		return ArgumentScoreboardCriteria.a(cmdCtx, key).getName();
	}

	@Override
	public OfflinePlayer getOfflinePlayer(CommandContext<CommandListenerWrapper> cmdCtx, String str)
			throws CommandSyntaxException {
		OfflinePlayer target = Bukkit
				.getOfflinePlayer((ArgumentProfile.a(cmdCtx, str).iterator().next()).getId());
		if (target == null) {
			throw ArgumentProfile.a.create();
		} else {
			return target;
		}
	}

	@Override
	public ParticleData<?> getParticle(CommandContext<CommandListenerWrapper> cmdCtx, String str) {
		final ParticleParam particleOptions = ArgumentParticle.a(cmdCtx, str);

		final Particle particle = CraftParticle.toBukkit(particleOptions);
		if (particleOptions instanceof ParticleParamBlock options) {
			IBlockData blockData = (IBlockData) ParticleParamBlock_c.get(options);
			return new ParticleData<BlockData>(particle, CraftBlockData.fromData(blockData));
		}
		if (particleOptions instanceof ParticleParamRedstone options) {
			String optionsStr = options.a(); // Of the format "particle_type float float float"
			String[] optionsArr = optionsStr.split(" ");
			final float red = Float.parseFloat(optionsArr[1]);
			final float green = Float.parseFloat(optionsArr[2]);
			final float blue = Float.parseFloat(optionsArr[3]);

			final Color color = Color.fromRGB((int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F));
			return new ParticleData<DustOptions>(particle,
					new DustOptions(color, (float) ParticleParamRedstone_f.get(options)));
		}
		if (particleOptions instanceof ParticleParamItem options) {
			return new ParticleData<org.bukkit.inventory.ItemStack>(particle,
					CraftItemStack.asBukkitCopy((ItemStack) ParticleParamItem_c.get(options)));
		}
		CommandAPI.getLogger().warning("Invalid particle data type for " + particle.getDataType().toString());
		return new ParticleData<Void>(particle, null);
	}

	@Override
	public Player getPlayer(CommandContext<CommandListenerWrapper> cmdCtx, String str) throws CommandSyntaxException {
		Player target = Bukkit.getPlayer((ArgumentProfile.a(cmdCtx, str).iterator().next()).getId());
		if (target == null) {
			throw ArgumentProfile.a.create();
		} else {
			return target;
		}
	}

	@Override
	public PotionEffectType getPotionEffect(CommandContext<CommandListenerWrapper> cmdCtx, String str)
			throws CommandSyntaxException {
		return new CraftPotionEffectType(ArgumentMobEffect.a(cmdCtx, str));
	}

	@Override
	public ComplexRecipe getRecipe(CommandContext<CommandListenerWrapper> cmdCtx, String key)
			throws CommandSyntaxException {
		IRecipe<?> recipe = ArgumentMinecraftKeyRegistered.b(cmdCtx, key);
		return new ComplexRecipeImpl(fromMinecraftKey(recipe.getKey()), recipe.toBukkitRecipe());
	}

	@Override
	public Rotation getRotation(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		Vec2F vec = ArgumentRotation.a(cmdCtx, key).b(cmdCtx.getSource());
		return new Rotation(vec.i, vec.j);
	}

	@Override
	public ScoreboardSlot getScoreboardSlot(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		return new ScoreboardSlot(ArgumentScoreboardSlot.a(cmdCtx, key));
	}

	@Override
	public Collection<String> getScoreHolderMultiple(CommandContext<CommandListenerWrapper> cmdCtx, String key)
			throws CommandSyntaxException {
		return ArgumentScoreholder.b(cmdCtx, key);
	}

	@Override
	public String getScoreHolderSingle(CommandContext<CommandListenerWrapper> cmdCtx, String key)
			throws CommandSyntaxException {
		return ArgumentScoreholder.a(cmdCtx, key);
	}

	@Override
	public CommandSender getSenderForCommand(CommandContext<CommandListenerWrapper> cmdCtx, boolean isNative) {
		CommandListenerWrapper clw = cmdCtx.getSource();

		CommandSender sender = clw.getBukkitSender();
		Vec3D pos = clw.getPosition();
		Vec2F rot = clw.i();
		World world = getWorldForCSS(clw);
		Location location = new Location(world, pos.getX(), pos.getY(), pos.getZ(), rot.j, rot.i);

		Entity proxyEntity = clw.getEntity();
		CommandSender proxy = proxyEntity == null ? null : proxyEntity.getBukkitEntity();
		if (isNative || (proxy != null && !sender.equals(proxy))) {
			return new NativeProxyCommandSender(sender, proxy, location, world);
		} else {
			return sender;
		}
	}

	@Override
	public SimpleCommandMap getSimpleCommandMap() {
		return ((CraftServer) Bukkit.getServer()).getCommandMap();
	}

	@Override
	public Sound getSound(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		MinecraftKey minecraftKey = ArgumentMinecraftKeyRegistered.e(cmdCtx, key);
		for (CraftSound sound : CraftSound.values()) {
			try {
				if (CommandAPIHandler.getInstance().getField(CraftSound.class, "minecraftKey").get(sound)
						.equals(minecraftKey.getKey())) {
					return Sound.valueOf(sound.name());
				}
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public SuggestionProvider<CommandListenerWrapper> getSuggestionProvider(SuggestionProviders provider) {
		return switch (provider) {
			case FUNCTION -> (context, builder) -> {
				CustomFunctionData functionData = MINECRAFT_SERVER.getFunctionData();
				ICompletionProvider.a(functionData.g(), builder, "#");
				return ICompletionProvider.a(functionData.f(), builder);
			};
			case RECIPES -> CompletionProviders.b;
			case SOUNDS -> CompletionProviders.c;
			case ADVANCEMENTS -> (cmdCtx, builder) -> {
				return ICompletionProvider.a(
						MINECRAFT_SERVER.getAdvancementData().getAdvancements().stream().map(Advancement::getName),
						builder);
			};
			case LOOT_TABLES -> (cmdCtx, builder) -> {
				return ICompletionProvider.a(MINECRAFT_SERVER.getLootTableRegistry().a(), builder);
			};
			case BIOMES -> CompletionProviders.d;
			case ENTITIES -> CompletionProviders.e;
			default -> (context, builder) -> Suggestions.empty();
		};
	}

	@Override
	public SimpleFunctionWrapper[] getTag(NamespacedKey key) {
		List<CustomFunction> customFunctions = new ArrayList<>(
				MINECRAFT_SERVER.getFunctionData().b(new MinecraftKey(key.getNamespace(), key.getKey())).getTagged());
		SimpleFunctionWrapper[] result = new SimpleFunctionWrapper[customFunctions.size()];
		for (int i = 0, size = customFunctions.size(); i < size; i++) {
			result[i] = convertFunction(customFunctions.get(i));
		}
		return result;
	}

	@Override
	public Set<NamespacedKey> getTags() {
		Set<NamespacedKey> functions = new HashSet<>();
		for (MinecraftKey key : MINECRAFT_SERVER.getFunctionData().g()) {
			functions.add(fromMinecraftKey(key));
		}
		return functions;
	}

	@Override
	public String getTeam(CommandContext<CommandListenerWrapper> cmdCtx, String key) throws CommandSyntaxException {
		return ArgumentScoreboardTeam.a(cmdCtx, key).getName();
	}

	@Override
	public int getTime(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		return cmdCtx.getArgument(key, Integer.class);
	}

	@Override
	public UUID getUUID(CommandContext<CommandListenerWrapper> cmdCtx, String key) {
		return ArgumentUUID.a(cmdCtx, key);
	}

	@Override
	public World getWorldForCSS(CommandListenerWrapper clw) {
		return (clw.getWorld() == null) ? null : clw.getWorld().getWorld();
	}

	@Override
	public boolean isVanillaCommandWrapper(Command command) {
		return command instanceof VanillaCommandWrapper;
	}

	@Override
	public void reloadDataPacks() {
		CommandAPI.getLogger().info("Reloading datapacks...");

		// Get previously declared recipes to be re-registered later
		Iterator<Recipe> recipes = Bukkit.recipeIterator();

		// Update the commandDispatcher with the current server's commandDispatcher
		DataPackResources datapackResources = MINECRAFT_SERVER.dataPackResources;
		datapackResources.commandDispatcher = MINECRAFT_SERVER.getCommandDispatcher();

		// Update the CustomFunctionManager for the datapackResources which now has the
		// new commandDispatcher
		try {
			CommandAPIHandler.getInstance().getField(CustomFunctionManager.class, "h").set(datapackResources.a(),
					getBrigadierDispatcher());
		} catch (IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}

		// Construct the new CompletableFuture that now uses our updated
		// datapackResources
		CompletableFuture<Unit> unitCompletableFuture = ((IReloadableResourceManager) DataPackResources_b
				.get(datapackResources)).a(SystemUtils.f(), Runnable::run,
						MINECRAFT_SERVER.getResourcePackRepository().f(), CompletableFuture.completedFuture(null));

		CompletableFuture<DataPackResources> completablefuture = unitCompletableFuture
				.whenComplete((Unit u, Throwable t) -> {
					if (t != null) {
						datapackResources.close();
					}
				}).thenApply((Unit u) -> datapackResources);

		// Run the completableFuture and bind tags
		try {
			completablefuture.get().i();

			// Register recipes again because reloading datapacks removes all non-vanilla
			// recipes
			Recipe recipe;
			while (recipes.hasNext()) {
				recipe = recipes.next();
				try {
					Bukkit.addRecipe(recipe);
					if (recipe instanceof Keyed keyedRecipe) {
						CommandAPI.logInfo("Re-registering recipe: " + keyedRecipe.getKey());
					}
				} catch (Exception e) {
					continue; // Can't re-register registered recipes. Not an error.
				}
			}

			CommandAPI.getLogger().info("Finished reloading datapacks");
		} catch (Exception e) {
			StringWriter stringWriter = new StringWriter();
			PrintWriter printWriter = new PrintWriter(stringWriter);
			e.printStackTrace(printWriter);

			CommandAPI.logError(
					"Failed to load datapacks, can't proceed with normal server load procedure. Try fixing your datapacks?\n"
							+ stringWriter.toString());
		}
	}

	@Override
	public void resendPackets(Player player) {
		MINECRAFT_SERVER.getCommandDispatcher().a(((CraftPlayer) player).getHandle());
	}

	@Override
	public Message generateMessageFromJson(String json) {
		return ChatSerializer.a(json);
	}

}
