package com.ms.bank.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperUtil {

    private ObjectMapperUtil () {}

    public static ObjectMapper getMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper;
    }
}
