package pt.upskill.groceryroutepro.utils;

public class Util {
    public static String capitalizeEveryWord(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] words = input.split("\\s+"); // Split the string into words
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                // Capitalize the first letter of each word
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" "); // Add a space between words
            }
        }

        // Remove the trailing space before returning the result
        return result.toString().trim();
    }
}
