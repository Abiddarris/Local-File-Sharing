package com.abiddarris.preferences;

public class ListEntry {

    private String title;
    private String value;

    public ListEntry(String title, String value) {
        this.title = title;
        this.value = value;
    }

    public String getTitle() {
        return this.title;
    }

    public String getValue() {
        return this.value;
    }

}
