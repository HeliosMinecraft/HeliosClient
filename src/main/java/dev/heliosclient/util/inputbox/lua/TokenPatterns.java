package dev.heliosclient.util.inputbox.lua;

import java.util.regex.Pattern;

public class TokenPatterns {
    public static final Pattern KEYWORD = Pattern.compile("\\b(and|break|do|else|elseif|end|for|function|if|in|local|nil|not|or|repeat|return|then|until|while)\\b");
    public static final Pattern BOOLEAN = Pattern.compile("\\b(true|false)\\b");
    public static final Pattern STRING = Pattern.compile("\".*?\"|'.*?'");
    public static final Pattern COMMENT = Pattern.compile("--.*");
    public static final Pattern NUMBER = Pattern.compile("\\b\\d+\\b");
    public static final Pattern VARIABLE_DECLARATION = Pattern.compile("\\blocal\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\b");
    public static final Pattern METHOD_CALL = Pattern.compile("[:.]\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\b");
    public static final Pattern VARIABLE_USAGE = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b"); //unused
    public static final Pattern WHITESPACE = Pattern.compile("\\s+");
}