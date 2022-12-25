package com.dotaherostatchecker;

public class Hero {

    private String name;
    private String id;
    private String localized_name;



    public Hero(String name, String id, String localized_name){
        this.name = name;
        this.id = id;
        this.localized_name = localized_name;
    }

    public Hero(){

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

    public String getLocalized_name() {
        return localized_name;
    }

    public void setLocalized_name(String localized_name) {
        this.localized_name = localized_name;
    }
}
