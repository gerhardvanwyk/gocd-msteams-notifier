package com.roxorgaming.gocd.msteams.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Utils {

    private static ObjectMapper mapper = new ObjectMapper();

    public static ObjectMapper getMapper(){
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
      //  mapper.getJsonFactory().setCharacterEscapes(new HTMLCharacterEscape());
        return  mapper;
    }
}
