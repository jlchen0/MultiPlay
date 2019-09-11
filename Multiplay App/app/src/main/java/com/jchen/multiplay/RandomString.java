package com.jchen.multiplay;

import java.security.SecureRandom;

public class RandomString {
    private int length;
    static final String validChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom random = new SecureRandom();

    public RandomString(int l){
        this.length = l;
    }

    public String generate(){
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++){
            result.append(validChars.charAt(random.nextInt(62)));

        }
        return result.toString();
    }


}
