package com.freelancedesk.model;

public class Freelancer extends Party {
    private final String profession;

    public Freelancer(int id, String name, String email, String profession) {
        super(id, name, email);
        this.profession = profession;
    }

    public String getProfession() { return profession; }

    @Override
    public String describe() {
        return String.format("[%d] %s | %s | %s", getId(), getName(), profession, getEmail());
    }
}
