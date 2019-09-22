package com.example.sfos.Model;

import android.util.Log;

import java.util.List;

public class RestaurantModel {

    private boolean success;
    private String message;
    private List<Restaurant> result;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {

        return message;

    }

    public void setMessage(String message) {
        this.message = message;
        Log.w("shahrukh", this.message);
    }

    public List<Restaurant> getResult() {
        return result;
    }

    public void setResult(List<Restaurant> result) {
        this.result = result;
    }
}
