package hu.martin.ems.base;

import java.util.Random;

public class RandomGenerator {
    private static final Random random = new Random();
    private static final int leftLimit = 97; // letter 'a'
    private static final int rightLimit = 122; // letter 'z'
    public static String generateRandomOnlyLetterString(){
        return generateRandomOnlyLetterString(10);
    }

    private static String generateRandomOnlyLetterString(int targetStringLength){
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

    public static CharSequence generateRandomEmailAddress() {
        String r = generateRandomOnlyLetterString(8) + "@" +
                generateRandomOnlyLetterString(5) + "." +
                generateRandomOnlyLetterString(3);
        return r;
    }
}
