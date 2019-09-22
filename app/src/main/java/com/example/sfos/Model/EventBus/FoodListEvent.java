package com.example.sfos.Model.EventBus;

import com.example.sfos.Model.Category;

public class FoodListEvent {
    private boolean success;
    private Category category;

    public FoodListEvent(boolean success, Category category) {
        this.success = success;
        this.category = category;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
