package fr.univavignon.alize.AndroidALIZEDemo.speakerslist;

import java.io.Serializable;

public class Speaker implements Serializable {
    private static final long serialVersionUID = -5435670920302756945L;

    private String id = "";
    private String name = "";

    public Speaker(String id, String name) {
        this.setName(name);
        this.setId(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}