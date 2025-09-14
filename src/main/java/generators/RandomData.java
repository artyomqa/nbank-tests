package generators;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomData {
    private RandomData() {};

    public static String getUsername() {
        return RandomStringUtils.random(10, 0, 0, true, false, null, new Random());
    }

    public static String getPassword() {
        List<String> symbols = new ArrayList<>();
        String[] allowedSpecialSymbols = new String[]{"!", "@", "#", "$", "%", "^", "&", "=", "+"};
        Random random = new Random();
        int passwordLength = random.nextInt(23) + 8;

        for (int i = 0; i < passwordLength; i++) {
            int randomInt = random.nextInt(4);

            if (i < 4) {
                randomInt = i;
            }

            if (randomInt == 0) {
                symbols.add(RandomStringUtils.random(1, 0, 0, true, false, null, random).toLowerCase());
            } else if (randomInt == 1) {
                symbols.add(RandomStringUtils.random(1, 0, 0, true, false, null, random).toUpperCase());
            } else if (randomInt == 2) {
                symbols.add(RandomStringUtils.random(1, 0, 0, false, true, null, random));
            } else {
                symbols.add(allowedSpecialSymbols[random.nextInt(allowedSpecialSymbols.length)]);
            }
        }

        Collections.shuffle(symbols);
        return String.join("", symbols);
    }
}
