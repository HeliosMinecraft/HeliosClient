package dev.heliosclient.util.inputbox.lua;

import dev.heliosclient.util.ColorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LuaSyntaxManager {
    private final List<String> keywords;
    public LuaSyntaxManager() {
        this.keywords = Arrays.asList("and", "break", "do", "else", "elseif", "end", "for", "function", "if", "in", "local", "nil", "not", "or", "repeat", "return", "then", "until", "while");
    }

    public static String applySyntaxHighlighting(String code) {
        LuaLexer lexer = new LuaLexer(code);
        List<Token> tokens = lexer.tokenize();
        StringBuilder highlightedCode = new StringBuilder();

        for (Token token : tokens) {
            String color = getColor(token.type());
            highlightedCode.append(color).append(token.value()).append(ColorUtils.reset);
        }

        return highlightedCode.toString();
    }

    private static String getColor(TokenType type) {
        return switch (type) {
            case STRING -> ColorUtils.green;
            case COMMENT -> ColorUtils.gray;
            case KEYWORD -> ColorUtils.red;
            case BOOLEAN -> ColorUtils.aqua;
            case VARIABLE_DECLARATION -> ColorUtils.magenta;
            case METHOD_CALL -> ColorUtils.gold;
            case NUMBER -> ColorUtils.blue;
            case WHITESPACE -> ""; // No color for whitespace
            default -> ColorUtils.reset;
        };
    }

    public List<String> getAutoCompleteSuggestions(String prefix) {
        List<String> suggestions = new ArrayList<>();
        for (String keyword : keywords) {
            if (keyword.startsWith(prefix)) {
                suggestions.add(keyword);
            }
        }
        return suggestions;
    }
}

