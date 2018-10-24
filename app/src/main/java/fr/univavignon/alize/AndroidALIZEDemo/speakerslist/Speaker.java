package fr.univavignon.alize.AndroidALIZEDemo.speakerslist;

import java.io.Serializable;

public class Speaker implements Serializable {
    private static final long serialVersionUID = -5435670920302756945L;

    private String name = "";

    public Speaker(String name) {
        this.setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}