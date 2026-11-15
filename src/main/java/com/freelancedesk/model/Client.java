package com.freelancedesk.model;

public class Client extends Party {
    private final String company;

    public Client(int id, String name, String email, String company) {
        super(id, name, email);
        this.company = company;
    }

    public String getCompany() { return company; }

    @Override
    public String describe() {
        return String.format("[%d] %s | %s | %s", getId(), getName(), company, getEmail());
    }
}
