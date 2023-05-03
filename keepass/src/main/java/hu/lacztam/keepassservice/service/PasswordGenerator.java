package hu.lacztam.keepassservice.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordGenerator {

    private int length;
    private boolean hasLowerCase;
    private boolean hasUpperCase;
    private boolean hasDigits;
    private boolean hasHyphen;
    private boolean underScore;
    private boolean hasSpace;
    private boolean hasSpecialCharacters;
    private boolean hasBrackets;

    public String generatePassword(){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < this.length; i++){
            stringBuilder.append(randomCharacterFromPool());
        }

        return stringBuilder.toString();
    }

    private char randomCharacterFromPool(){
        int spaceChar = 32;
        int hyphen = 45;
        int underScore = 95;
        List<Integer> brackets = Arrays.asList(40,41,91,93,123,125,60,62);
        List<Integer> specialCharacters
                = Arrays.asList(33,34,35,36,37,38,39,42,43,44,46,47,58,59,61,63,64,92,94,96,124,126);

        List<Integer> finalList = new ArrayList<>();
        if(this.hasLowerCase)
            finalList.addAll(fillLowerCaseChars());

        if(this.hasUpperCase)
            finalList.addAll(fillUpperCaseChars());

        if(this.hasDigits)
            finalList.addAll(fillDigits());

        if(this.hasSpecialCharacters)
            finalList.addAll(specialCharacters);

        if(this.hasHyphen)
            finalList.add(hyphen);

        if(this.underScore)
            finalList.add(underScore);

        if(this.hasSpace)
            finalList.add(spaceChar);

        if(this.hasBrackets)
            finalList.addAll(brackets);

        Collections.sort(finalList);

        Random random = new Random();
        int leftLimit = finalList.get(0);
        int rightLimit = finalList.get(finalList.size() - 1);
        int randomLimitedInt = finalList.get(0) + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
        char randomCharacter = (char) randomLimitedInt;

        return randomCharacter;
    }

    private List<Integer> fillDigits() {
        List<Integer> digits = new ArrayList<>();
        for(int i = 48; i <= 57; i++)
            digits.add(i);

        return digits;
    }

    private List<Integer> fillUpperCaseChars() {
        List<Integer> upperCaseChars = new ArrayList<>();
        for(int i = 65; i <= 90; i++)
            upperCaseChars.add(i);

        return upperCaseChars;
    }

    private List<Integer> fillLowerCaseChars(){
        List<Integer> lowerCaseCharacters = new ArrayList<>();
        for(int i = 97; i <= 122; i++)
            lowerCaseCharacters.add(i);

        return lowerCaseCharacters;
    }
}
