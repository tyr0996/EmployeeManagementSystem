package hu.martin.ems.base;

import java.util.Random;

public class RandomGenerator {
    private static final Random random = new Random();
    public static String generateRandomOnlyLetterString(){
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

    public static Integer generateRandomInteger(){
        return random.nextInt(0, 100000);
    }
}
