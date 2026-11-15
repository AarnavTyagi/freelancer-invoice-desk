package com.freelancedesk.model;

/** Common base for the people who appear on an invoice. */
public abstract class Party {
    private int id;
    private final String name;
    private final String email;

    protected Party(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    /** One-line summary used by the console menus. */
    public abstract String describe();
}
