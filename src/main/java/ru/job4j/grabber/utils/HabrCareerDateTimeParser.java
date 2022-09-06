package ru.job4j.grabber.utils;

import java.time.LocalDateTime;
import java.util.Arrays;

public class HabrCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String parse) {
        String[] dateTime = parse.split("\\D");
        Integer[] numVal = Arrays.stream(dateTime)
                .limit(6)
                .map(Integer :: parseInt)
                .toArray(Integer[] :: new);
        return LocalDateTime.of(numVal[0], numVal[1], numVal[2], numVal[3], numVal[4], numVal[5]);
    }

}
