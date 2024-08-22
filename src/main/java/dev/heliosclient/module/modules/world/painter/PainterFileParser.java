package dev.heliosclient.module.modules.world.painter;

import com.ibm.icu.impl.InvalidFormatException;
import dev.heliosclient.HeliosClient;
import dev.heliosclient.util.blocks.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;

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
    public static String POSITION_BLOCK_REGEX = "\\[-?\\d+,-?\\d+,-?\\d+\\]\\{[a-zA-Z0-9_]+:[a-zA-Z0-9_]+\\}";

    public static LinkedHashMap<BlockPos, Block> parseFilePath(Path filePath) {
        LinkedHashMap<BlockPos, Block> result = new LinkedHashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.matches(POSITION_BLOCK_REGEX)) {
                    readLine(line.trim(), result);
                }
            }
        } catch (IOException | InvalidIdentifierException | NumberFormatException | InvalidFormatException e) {
            HeliosClient.LOGGER.error("Error while parsing painter file {}", filePath, e);
            return null;
        }
        return result;
    }

    public static LinkedHashMap<BlockPos, Block> parseString(String string) {
        LinkedHashMap<BlockPos, Block> result = new LinkedHashMap<>();
        String[] lines = string.split("\n");
        for (String line : lines) {
            try {
                readLine(line.trim(), result);
            }catch (NumberFormatException | InvalidFormatException e){
                HeliosClient.LOGGER.error("Error while parsing string {} for Painter Module", string, e);
            }
        }

        return result;
    }

    public static LinkedHashMap<BlockPos, Block> parseFile(File file) {
        if (file == null) {
            return null;
        }
        return PainterFileParser.parseFilePath(file.toPath());
    }

    private static void readLine(String line, HashMap<BlockPos, Block> result) throws InvalidFormatException {
        if (line.isEmpty()) return;

        String[] parts = line.split("\\]\\{");

        if (parts.length < 2) {
            throw new InvalidFormatException("Line found empty or incomplete");
        }

        String posString = parts[0].substring(1); // Remove leading '['
        String blockString = parts[1].substring(0, parts[1].length() - 1); // Remove trailing '}'
        String[] coordinates = posString.split(",");
        if (coordinates.length == 3) {
            try {
                int x = Integer.parseInt(coordinates[0].trim());
                int y = Integer.parseInt(coordinates[1].trim());
                int z = Integer.parseInt(coordinates[2].trim());
                BlockPos pos = new BlockPos(x, y, z);

                Block block = BlockUtils.getBlockFromString(blockString);
                result.put(pos, block);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Incorrect formatting of block coordinates in painter file");
            }
        } else {
            throw new NumberFormatException("Incorrect formatting of block coordinates in painter file");
        }
    }

}
