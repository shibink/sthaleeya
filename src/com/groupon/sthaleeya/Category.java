package com.groupon.sthaleeya;

public enum Category {
    ALL(0),
    FOOD(1),
    MOVIES(2);
    
    private int category;
    Category(int value) {
        category = value;
    }
    
    public int value() {
        return category;
    }
}
