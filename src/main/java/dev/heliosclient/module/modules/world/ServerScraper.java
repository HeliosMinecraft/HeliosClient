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
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ServerScraper extends Module_ {
    SettingGroup sgGeneral = new SettingGroup("AutoFill");
    MutableText separator = Text.of("=======================================================").copy().formatted(Formatting.DARK_GREEN);

    BooleanSetting obfuscatePlayerInfo = sgGeneral.add(new BooleanSetting.Builder()
            .name("Obfuscate player info")
            .description("It will obfuscate player info like its spawn pos, yaw, pitch, and facing direction. WARNING: This wont stop the text from being visible in your log files!!!")
            .onSettingChange(this)
            .defaultValue(false)
            .build()
    );
    boolean resourcePackURLSent = false;
    String lastURL = "";

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

            Text text = Text.literal("ResourcePack URL:")
                    .formatted(Formatting.BOLD, Formatting.BLUE)
                    .append(Text.literal(" " + rsPack.url())
                            .formatted(Formatting.ITALIC, Formatting.GOLD)
                    );

            ChatUtils.sendHeliosMsg(text);
            resourcePackURLSent = true;
            lastURL = rsPack.url();
        }
    }
    public void sendGameJoinInfoMessage(GameJoinS2CPacket joinPacket){
        ChatUtils.sendHeliosMsg(separator);

        String lastDeathPos = String.valueOf(joinPacket.commonPlayerSpawnInfo().lastDeathLocation().orElse(GlobalPos.create(World.OVERWORLD,new BlockPos(0,0,0))));
        String playerEntityID = String.valueOf(joinPacket.playerEntityId());
        String seed = String.valueOf(joinPacket.commonPlayerSpawnInfo().seed());
        boolean reducedDebugInfo = joinPacket.reducedDebugInfo();
        boolean isHardCore = joinPacket.hardcore();
        String maxPlayers = String.valueOf(joinPacket.maxPlayers());

        Text joinPacketInfo = Text.literal("Join Info:")
                .formatted(Formatting.BOLD, Formatting.GREEN)
                .append(Text.literal("\nLast Death Pos: ").formatted(Formatting.WHITE).append(Text.literal(lastDeathPos).formatted(Formatting.YELLOW)))
                .append(Text.literal("\nIs HardCore: ").formatted(Formatting.WHITE).append(Text.literal(isHardCore ? "Yes" : "No").formatted(isHardCore ? Formatting.GREEN : Formatting.RED)))
                .append(Text.literal("\nHas Reduced Debug Info: ").formatted(Formatting.WHITE).append(Text.literal(reducedDebugInfo ? "Yes" : "No").formatted(reducedDebugInfo ? Formatting.GREEN : Formatting.RED)))
                .append(Text.literal("\nMax Players: ").formatted(Formatting.WHITE).append(Text.literal(maxPlayers).formatted(Formatting.GREEN)))
                .append(Text.literal("\nSeed: ").formatted(Formatting.WHITE).append(Text.literal(seed).formatted(Formatting.GREEN)))
                .append(Text.literal("\nPlayer Entity ID: ").formatted(Formatting.WHITE).append(Text.literal(playerEntityID).formatted(Formatting.YELLOW)));

        ChatUtils.sendHeliosMsg(joinPacketInfo);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Get the current world
        ClientWorld world =  mc.world;

        if (world != null) {
            sendGameJoinInfoMessage(event.getPacket());

            ChatUtils.sendHeliosMsg(separator);

            // Gather world information
            long worldAge = world.getTime();
            long dayTime = world.getTimeOfDay();
            boolean isDay = dayTime >= 0 && dayTime < 12000; // Daytime is between 0 and 12000 ticks

            // Gather player and entity information
            int playerCount = world.getPlayers().size();

            // Gather world border information
            double worldBorderSize = world.getWorldBorder().getSize();
            Vec3d worldBorderCenter = new Vec3d(world.getWorldBorder().getCenterX(),0, world.getWorldBorder().getCenterZ());

            // Gather biome information
            String biomeName = world.getBiome(mc.player.getBlockPos()).getKey().orElseThrow().getValue().toString();
            Formatting obfuscated = obfuscatePlayerInfo.value ? Formatting.OBFUSCATED : Formatting.RESET ;

            Text yawAndPitch = Text.literal("\nPlayer Yaw And Pitch: ")
                    .formatted(Formatting.WHITE)
                    .append(Text.literal("Yaw: ").formatted(Formatting.YELLOW))
                    .append(Text.literal(String.valueOf(mc.player.getYaw())).formatted(Formatting.GREEN))
                    .append(Text.literal(" Pitch: ").formatted(Formatting.YELLOW))
                    .append(Text.literal(String.valueOf(mc.player.getPitch())).formatted(Formatting.GREEN))
                    .formatted(obfuscated);


            // Constructing the information message
            Text worldInfoMessage = Text.literal("World Info:")
                    .formatted(Formatting.BOLD, Formatting.GREEN)
                    .append(Text.literal("\nWorld Age: ").formatted(Formatting.WHITE).append(Text.literal(worldAge + " ticks").formatted(Formatting.YELLOW)))
                    .append(Text.literal("\nDay Time: ").formatted(Formatting.WHITE).append(Text.literal(dayTime + " ticks").formatted(Formatting.YELLOW)))
                    .append(Text.literal("\nDay Count: ").formatted(Formatting.WHITE).append(Text.literal(String.valueOf(Math.floor(mc.world.getTime() / 24000))).formatted(Formatting.YELLOW)))
                    .append(Text.literal("\nIs Day: ").formatted(Formatting.WHITE).append(Text.literal(isDay ? "Yes" : "No").formatted(isDay ? Formatting.GREEN : Formatting.RED)))
                    .append(Text.literal("\nPlayer Count: ").formatted(Formatting.WHITE).append(Text.literal(playerCount + "").formatted(Formatting.YELLOW)))
                    .append(Text.literal("\nWorld Border Size: ").formatted(Formatting.WHITE).append(Text.literal(worldBorderSize + " blocks").formatted(Formatting.YELLOW)))
                    .append(Text.literal("\nWorld Border Center: ").formatted(Formatting.WHITE).append(Text.literal(worldBorderCenter.toString()).formatted(Formatting.YELLOW)))
                    .append(Text.literal("\nDifficulty: ").formatted(Formatting.WHITE).append(Text.literal(mc.world.getDifficulty().name()).formatted(Formatting.YELLOW)))
                    .append(Text.literal("\nPermissionLevel: ").formatted(Formatting.WHITE).append(Text.literal(String.valueOf(mc.player.getPermissionLevel())).formatted(Formatting.YELLOW)))
                    .append(Text.literal("\nCurrent Biome: ").formatted(Formatting.WHITE).append(Text.literal(biomeName).formatted(Formatting.YELLOW)))
                    .append(Text.literal("\nWorld Spawn Pos: ").formatted(Formatting.WHITE).append(Text.literal(mc.world.getSpawnPos().toString()).formatted(Formatting.YELLOW)))
                    .append(Text.literal("\nPlayer Spawn Pos: ").formatted(Formatting.WHITE).append(Text.literal(mc.player.getBlockPos().toString()).formatted(Formatting.YELLOW,obfuscated)))
                    .append(Text.literal("\nPlayer Facing Direction: ").formatted(Formatting.WHITE).append(Text.literal(mc.player.getHorizontalFacing().name()).formatted(Formatting.YELLOW, obfuscated)))
                    .append(yawAndPitch);

            // Display the message in chat
            ChatUtils.sendHeliosMsg(worldInfoMessage);
            ChatUtils.sendHeliosMsg(Text.of("------------------------------------------------------"));
            //mc.inGameHud.getChatHud().addMessage(worldInfoMessage);
           // mc.inGameHud.getChatHud().addMessage(Text.of("------------------------------------------------------"));


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
                        .append(Text.literal("\nName: ").formatted(Formatting.WHITE).append(Text.literal(serverName).formatted(Formatting.YELLOW)))
                        .append(Text.literal("\nServer Address: ").formatted(Formatting.WHITE).append(Text.literal(serverAddress).formatted(Formatting.YELLOW)))
                        .append(Text.literal("\nVersion: ").formatted(Formatting.WHITE).append(Text.literal(serverVersion).formatted(Formatting.YELLOW)))
                        .append(Text.literal("\nProtocol Version: ").formatted(Formatting.WHITE).append(Text.literal(serverProtocolVersion).formatted(Formatting.YELLOW)))
                        .append(Text.literal("\nMOTD: ").formatted(Formatting.WHITE).append(Text.literal(motd).formatted(Formatting.YELLOW)))
                        .append(Text.literal("\nOnline: ").formatted(Formatting.WHITE).append(Text.literal(online).formatted(Formatting.YELLOW)))
                        .append(Text.literal("\nTexture Pack Required: ").formatted(Formatting.WHITE).append(Text.literal(texturePackRequired ? "Yes" : "No").formatted(texturePackRequired ? Formatting.GREEN : Formatting.RED)))
                        .append(Text.literal("\nTexture Pack Policy: ").formatted(Formatting.WHITE).append(Text.literal(info.getResourcePackPolicy().name()).formatted(Formatting.YELLOW)))
                        .append(Text.literal("\nIs Chat Secure: ").formatted(Formatting.WHITE).append(Text.literal(hasSecureChat ? "Yes" : "No").formatted(hasSecureChat ? Formatting.GREEN : Formatting.RED)))
                        .append(Text.literal("\nServer Type: ").formatted(Formatting.WHITE).append(Text.literal(info.getServerType().name()).formatted(Formatting.YELLOW)));

                ChatUtils.sendHeliosMsg(serverInfoMessage);
            }

            ChatUtils.sendHeliosMsg(separator);
        }
    }
}
