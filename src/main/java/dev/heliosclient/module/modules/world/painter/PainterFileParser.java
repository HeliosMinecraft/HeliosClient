package dev.heliosclient.module.modules.world.painter;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class PainterFileParser {

    /**
     * Current Regex style:
     * <p>
     * <h3> [x,y,z]{block} </h3>
     * <p>
     * Example:
     * <pre>
     * <code>[0,1,0]{minecraft:bedrock}</code>
     */
    public static String POSITION_BLOCK_REGEX = "\\[\\d+,\\d+,\\d+\\]\\{[a-zA-Z0-9_]+:[a-zA-Z0-9_]+\\}";


    public static HashMap<BlockPos, Block> parseFilePath(Path filePath) {
        HashMap<BlockPos, Block> result = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.matches(POSITION_BLOCK_REGEX)) {
                    readLine(line, result);
                }
            }
        } catch (IOException | InvalidIdentifierException | NumberFormatException e) {
            HeliosClient.LOGGER.error("Error while parsing painter file {}", filePath, e);
            return null;
        }
        return result;
    }

    public static HashMap<BlockPos, Block> parseString(String string) {
        HashMap<BlockPos, Block> result = new HashMap<>();
        String[] lines = string.split("\n");
        for (String line : lines) {
            readLine(line, result);
        }

        return result;
    }

    public static HashMap<BlockPos, Block> parseFile(File file) {
        if (file == null) {
            return null;
        }
        return PainterFileParser.parseFilePath(file.toPath());
    }

    private static void readLine(String line, HashMap<BlockPos, Block> result) {
        String[] parts = line.split("\\]\\{");
        String posString = parts[0].substring(1); // Remove leading '['
        String blockString = parts[1].substring(0, parts[1].length() - 1); // Remove trailing '}'
        String[] coordinates = posString.split(",");
        if (coordinates.length == 3) {
            int x = Integer.parseInt(coordinates[0].trim());
            int y = Integer.parseInt(coordinates[1].trim());
            int z = Integer.parseInt(coordinates[2].trim());
            BlockPos pos = new BlockPos(x, y, z);

            Block block = BlockUtils.getBlockFromString(blockString);
            result.put(pos, block);
        } else {
            throw new NumberFormatException("Incorrect formatting of block coordinates in painter file");
        }
    }
}
