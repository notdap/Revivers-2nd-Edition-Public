package net.revivers.reviverstwo.utilities;

import java.util.Random;

public final class RandomText
{

    private final String generatedPassword;

    public RandomText(int length)
    {
        StringBuilder password = new StringBuilder(length);
        Random random = new Random(System.nanoTime());

        for (int i = 0; i < length; i++)
        {

            String[] CHAR_CATEGORIES = new String[] {
                    "abcdefghijklmnopqrstuvwxyz",
                    "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                    "0123456789"
            };

            String charCategory = CHAR_CATEGORIES[random.nextInt(CHAR_CATEGORIES.length)];
            int position = random.nextInt(charCategory.length());
            password.append(charCategory.charAt(position));
        }

        generatedPassword = new String(password);
    }

    public String get() {
        return generatedPassword;
    }

}
