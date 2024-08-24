package Lexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Lexer class responsible for tokenizing input
public class Lexer {
    private String input;
    private List<Token> tokens;

    // Constructor to initialize Lexer with input
    public Lexer(String input) {
        this.input = input;
        tokens = new ArrayList<>();
    }

    // Tokenize the input
    public List<Token> tokenize() {
        try (BufferedReader reader = new BufferedReader(new FileReader(input))) {
            String line;
            int lineCount = 0;
            // Read each line and process
            while ((line = reader.readLine()) != null) {
                lineCount++;
                processLine(line, lineCount);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tokens;
    }

    // Process each line of input
    private void processLine(String line, int lineCount) {
        String digitRegex = "[0-9]";
        String letterRegex = "[a-zA-Z]";
        Pattern operatorSymbolPattern = Pattern.compile("[+\\-*/<>&.@/:=~|$!#%^_\\[\\]{}\"`\\?]");
        Pattern escapePattern = Pattern.compile("(\\\\'|\\\\t|\\\\n|\\\\\\\\)");

        Pattern identifierPattern = Pattern.compile(letterRegex + "(" + letterRegex + "|" + digitRegex + "|" + "_)*");
        Pattern integerPattern = Pattern.compile(digitRegex + "+");
        Pattern operatorPattern = Pattern.compile(operatorSymbolPattern + "+");

        Pattern punctuationPattern = Pattern.compile("[(),;]");
        Pattern spacesPattern = Pattern.compile("(\\s|\\t)+");

        Pattern stringPattern = Pattern.compile("'(" + letterRegex + "|" + digitRegex + "|" + operatorSymbolPattern + "|" + escapePattern + "|"
                + punctuationPattern + "|" + spacesPattern + ")*'");
        Pattern commentPattern = Pattern.compile("//.*");

        Matcher matcher;

        int currentIndex = 0;
        // Loop through the line to process each character
        while (currentIndex < line.length()) {
            char currentChar = line.charAt(currentIndex);
            Matcher spaceMatcher = spacesPattern.matcher(line.substring(currentIndex));
            Matcher commentMatcher = commentPattern.matcher(line.substring(currentIndex));
            if (commentMatcher.lookingAt()) {
                // If a comment is found, skip it
                String comment = commentMatcher.group();
                currentIndex += comment.length();
                continue;
            }
            if (spaceMatcher.lookingAt()) {
                // If a space is found, skip it
                String space = spaceMatcher.group();
                currentIndex += space.length();
                continue;
            }

            matcher = identifierPattern.matcher(line.substring(currentIndex));
            if (matcher.lookingAt()) {
                // If identifier pattern is matched
                String identifier = matcher.group();
                List<String> keywords = List.of("let", "in", "fn", "where", "aug", "or", "not", "gr", "ge", "ls",
                        "le", "eq", "ne", "true", "false", "nil", "dummy", "within", "and", "rec");
                // Check if identifier is a keyword
                if (keywords.contains(identifier))
                    tokens.add(new Token(TokenType.KEYWORD, identifier));
                else
                    tokens.add(new Token(TokenType.IDENTIFIER, identifier));
                currentIndex += identifier.length();
                continue;
            }
            matcher = integerPattern.matcher(line.substring(currentIndex));
            if (matcher.lookingAt()) {
                // If integer pattern is matched
                String integer = matcher.group();
                tokens.add(new Token(TokenType.INTEGER, integer));
                currentIndex += integer.length();
                continue;
            }
            matcher = operatorPattern.matcher(line.substring(currentIndex));
            if (matcher.lookingAt()) {
                // If operator pattern is matched
                String operator = matcher.group();
                tokens.add(new Token(TokenType.OPERATOR, operator));
                currentIndex += operator.length();
                continue;
            }
            matcher = stringPattern.matcher(line.substring(currentIndex));
            if (matcher.lookingAt()) {
                // If string pattern is matched
                String string = matcher.group();
                tokens.add(new Token(TokenType.STRING, string));
                currentIndex += string.length();
                continue;
            }
            matcher = punctuationPattern.matcher(Character.toString(currentChar));
            if (matcher.matches()) {
                // If punctuation pattern is matched
                tokens.add(new Token(TokenType.PUNCTUATION, Character.toString(currentChar)));
                currentIndex++;
                continue;
            }

            // If none of the patterns match, throw exception
            throw new RuntimeException("Unable to tokenize");
        }
    }
}
