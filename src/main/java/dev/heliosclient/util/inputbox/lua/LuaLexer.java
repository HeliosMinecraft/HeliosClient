package dev.heliosclient.util.inputbox.lua;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuaLexer {
    private final String input;
    private int position;
    private final List<String> definedVariables;

    public LuaLexer(String input) {
        this.input = input;
        this.position = 0;
        this.definedVariables = new ArrayList<>();
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (position < input.length()) {
            Token token = nextToken();
            if (token != null) {
                tokens.add(token);
                if (token.type() == TokenType.VARIABLE_DECLARATION) {
                    definedVariables.add(token.value());
                }
            }
        }
        return tokens;
    }

    private Token nextToken() {
        if (position >= input.length()) {
            return null;
        }

        String remainingInput = input.substring(position);

        for (TokenType type : TokenType.values()) {
            Pattern pattern = getPattern(type);
            if (pattern != null) {
                Matcher matcher = pattern.matcher(remainingInput);
                if (matcher.lookingAt()) {
                    String value = matcher.group();
                    position += value.length();
                    return new Token(type, value);
                }
            }
        }

        // If no pattern matches, consume one character as an unknown token
        position++;
        return new Token(TokenType.UNKNOWN, remainingInput.substring(0, 1));
    }

    private Pattern getPattern(TokenType type) {
        return switch (type) {
            case STRING -> TokenPatterns.STRING;
            case COMMENT -> TokenPatterns.COMMENT;
            case KEYWORD -> TokenPatterns.KEYWORD;
            case BOOLEAN -> TokenPatterns.BOOLEAN;
            case VARIABLE_DECLARATION -> TokenPatterns.VARIABLE_DECLARATION;
            case METHOD_CALL -> TokenPatterns.METHOD_CALL;
            case NUMBER -> TokenPatterns.NUMBER;
            case WHITESPACE -> TokenPatterns.WHITESPACE;
            default -> null;
        };
    }

    public List<String> getDefinedVariables() {
        return definedVariables;
    }
}
