package dev.heliosclient.module.modules.world;

import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.player.PacketEvent;
import dev.heliosclient.event.events.player.PlayerJoinEvent;
import dev.heliosclient.module.Categories;
import dev.heliosclient.module.Module_;
import dev.heliosclient.module.settings.BooleanSetting;
import dev.heliosclient.module.settings.SettingGroup;
import dev.heliosclient.util.ChatUtils;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ServerScraper extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("General");

    BooleanSetting obfuscatePlayerInfo = sgGeneral.add(new BooleanSetting.Builder()
            .name("Obfuscate player info")
            .description("It will obfuscate your data like spawn pos, yaw, pitch, and facing direction. WARNING: This wont stop the text from being visible in your log files!!!")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );

    final MutableText separator = Text.literal("\n=======================================================\n").formatted(Formatting.DARK_GREEN);
    private boolean resourcePackURLSent = false;
    private String lastURL = "";

    public ServerScraper() {
        super("ServerScraper", "Gathers information about the server and world on join.", Categories.WORLD);
        addSettingGroup(sgGeneral);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.RECEIVE event) {
        if (event.getPacket() instanceof ResourcePackSendS2CPacket rsPack) {
            if(!rsPack.url().equals(lastURL)){
                resourcePackURLSent = false;
            }

            if(resourcePackURLSent) return;

            Text text = Text.literal("ResourcePack URL: ")
                    .formatted(Formatting.BOLD, Formatting.BLUE)
                    .append(Text.literal(rsPack.url()).formatted(Formatting.ITALIC, Formatting.GOLD));

            ChatUtils.sendHeliosMsg(text);
            resourcePackURLSent = true;
            lastURL = rsPack.url();
        }
    }

    public void sendGameJoinInfoMessage(GameJoinS2CPacket joinPacket){
        String lastDeathPos = joinPacket.commonPlayerSpawnInfo().lastDeathLocation().orElse(GlobalPos.create(World.OVERWORLD, new BlockPos(0, 0, 0))).toString();
        String playerEntityID = String.valueOf(joinPacket.playerEntityId());
        String seed = String.valueOf(joinPacket.commonPlayerSpawnInfo().seed());
        String viewDistance = String.valueOf(joinPacket.viewDistance());

        boolean reducedDebugInfo = joinPacket.reducedDebugInfo();
        boolean isHardCore = joinPacket.hardcore();
        String maxPlayers = String.valueOf(joinPacket.maxPlayers());
        Text joinPacketInfo = separator.copy().append(Text.literal("Join Info:").formatted(Formatting.BOLD, Formatting.GREEN))
                .append(generateClickableText("Last Death Pos: ",lastDeathPos,Formatting.WHITE, Formatting.GREEN).formatted(obfuscatePlayerInfo.value ? Formatting.OBFUSCATED : Formatting.RESET))
                .append(generateText("Max Players: ",Formatting.WHITE, maxPlayers,Formatting.GREEN))
                .append(generateClickableText("Seed: ", seed, Formatting.WHITE, Formatting.GREEN))
                .append(generateText("View Distance: ",viewDistance))
                .append(generateText("Player Entity ID: ",playerEntityID))
                .append(generateYesNoText("Is HardCore: ",isHardCore))
                .append(generateYesNoText("Has Reduced Debug Info: ", reducedDebugInfo));

        ChatUtils.sendHeliosMsg(joinPacketInfo);
    }

    @SubscribeEvent(priority = SubscribeEvent.Priority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Get the current world
        ClientWorld world =  mc.world;

        if (world != null) {
            sendGameJoinInfoMessage(event.getPacket());

            // Gather world information
            long worldAge = world.getTime();
            long dayTime = world.getTimeOfDay();
            boolean isDay = dayTime >= 0 && dayTime < 12000; // Daytime is between 0 and 12000 ticks
            double dayCount = Math.floor(mc.world.getTime() / 24000);
            int nextMapId = world.getNextMapId();
            float tickRate = world.getTickManager().getTickRate();

            // Gather player and entity information
            int playerCount = world.getPlayers().size();

            // Gather world border information
            double worldBorderSize = world.getWorldBorder().getSize();
            Vec3d worldBorderCenter = new Vec3d(world.getWorldBorder().getCenterX(),0, world.getWorldBorder().getCenterZ());

            // Gather biome information
            String biomeName = world.getBiome(mc.player.getBlockPos()).getKey().orElseThrow().getValue().toString();
            Formatting obfuscated = obfuscatePlayerInfo.value ? Formatting.OBFUSCATED : Formatting.RESET;

            Text yawAndPitch = Text.literal("\nPlayer Yaw And Pitch: ")
                    .formatted(Formatting.WHITE)
                    .append(generateText("Yaw: ",Formatting.YELLOW, mc.player.getYaw() + "",Formatting.GREEN)
                            .formatted(obfuscated)
                    )
                    .append(generateText("Pitch: ",Formatting.YELLOW, mc.player.getPitch() + "",Formatting.GREEN)
                            .formatted(obfuscated)
                    );

            // Constructing the information message
            Text worldInfoMessage = Text.literal("World Info:")
                    .formatted(Formatting.BOLD, Formatting.GREEN)
                    .append(generateText("World Age: ",worldAge + " ticks"))
                    .append(generateText("Day Time: ",dayTime + " ticks"))
                    .append(generateText("Day Count: ",dayCount + ""))
                    .append(generateYesNoText("Is Day: ",isDay))
                    .append(generateText("Player Count: ",playerCount + ""))
                    .append(generateText("World Tick Rate: ",tickRate + ""))
                    .append(generateText("World Border Size: ",worldBorderSize + " blocks"))
                    .append(generateText("World Border Center: ",worldBorderCenter.toString()))
                    .append(generateText("Next Map ID: ", nextMapId + ""))
                    .append(generateText("Difficulty: ",mc.world.getDifficulty().name()))
                    .append(generateText("PermissionLevel: ",mc.player.getPermissionLevel() + ""))
                    .append(generateText("Current Biome: ",biomeName))
                    .append(generateText("World Spawn Pos: ",mc.world.getSpawnPos().toString()))
                    .append(generateText("Player Spawn Pos: ",mc.player.getBlockPos().toString()).formatted(obfuscated))
                    .append(generateText("Player Facing Direction: ",mc.player.getHorizontalFacing().name()).formatted(obfuscated))
                    .append(yawAndPitch)
                    .append(Text.of("\n------------------------------------------------------"));

            // Display the message in chat
            ChatUtils.sendHeliosMsg(worldInfoMessage);

            ServerInfo info = mc.getCurrentServerEntry();
            if(info != null) {
                String serverName = info.name;
                String serverVersion = info.version.getString();
                String serverProtocolVersion = String.valueOf(info.protocolVersion);
                String motd = info.label.getString();
                String serverAddress = info.address;
                String online = String.valueOf(info.online);
                boolean hasSecureChat = info.isSecureChatEnforced();
                boolean texturePackRequired = info.getResourcePackPolicy() == ServerInfo.ResourcePackPolicy.ENABLED;

                // Constructing the server information message
                MutableText serverInfoMessage = Text.literal("Server Info:")
                        .formatted(Formatting.BOLD, Formatting.BLUE)
                        .append(generateText("Name: ",serverName))
                        .append(generateText("Server Address: ",serverAddress))
                        .append(generateText("Version: ",serverVersion))
                        .append(generateText("Protocol Version: ",serverProtocolVersion))
                        .append(generateText("MOTD: ",motd))
                        .append(generateText("Online: ",online))
                        .append(generateYesNoText("Texture Pack Required: ",texturePackRequired))
                        .append(generateText("Texture Pack Policy: ",info.getResourcePackPolicy().name()))
                        .append(generateYesNoText("Is Chat Secure: ", hasSecureChat))
                        .append(generateText("Server Type: ",info.getServerType().name()));

                ChatUtils.sendHeliosMsg(serverInfoMessage);
            }

            ChatUtils.sendHeliosMsg(separator);
        }
    }

    public MutableText generateText(String prefix, Formatting prefixFormat, String value, Formatting valueFormat){
         return Text.literal("\n" + prefix).formatted(prefixFormat).append(Text.literal(value).formatted(valueFormat));
    }
    public MutableText generateText(String prefix, String value){
        return generateText(prefix,Formatting.WHITE,value,Formatting.YELLOW);
    }
    public MutableText generateYesNoText(String prefix, boolean bool){
        return generateText(prefix,Formatting.WHITE, bool ? "Yes" : "No", bool ? Formatting.GREEN : Formatting.RED);
    }
    private MutableText generateClickableText(String prefix, String value, Formatting labelColor, Formatting valueColor) {
        return Text.literal("\n" + prefix).formatted(labelColor)
                .append(Text.literal(value).formatted(valueColor)
                        .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value))));
    }
}
