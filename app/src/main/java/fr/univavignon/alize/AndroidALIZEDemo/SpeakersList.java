package fr.univavignon.alize.AndroidALIZEDemo;

import java.util.HashMap;
import java.util.Map;

class SpeakersList {
    private static Map<String, String> nameIdList;

    static Map<String, String> getList() {
        if (nameIdList == null) {
            nameIdList = new HashMap<>();
        }
        return nameIdList;
    }

    static void reset() {
        nameIdList = null;
    }
}
