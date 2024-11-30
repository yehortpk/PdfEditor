package com.github.yehortpk.renderer.services;

import com.google.common.hash.Hashing;
import lombok.Getter;

import java.nio.charset.StandardCharsets;

@Getter
public class HashService {
    private final int HASH_LENGTH = 10;
    private final String hashValue;


    public HashService(String inputValue) {
        hashValue = Hashing.sha256()
                .hashString(inputValue, StandardCharsets.UTF_8)
                .toString().substring(0, HASH_LENGTH);
    }

}
