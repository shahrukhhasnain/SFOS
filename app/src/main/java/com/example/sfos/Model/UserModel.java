package com.example.sfos.Model;

import android.util.Log;

import java.util.List;

public class UserModel {

    private boolean success;
    private String message;
    private List<User> result;

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
        Log.w("shahrukh", message);
    }

    public List<User> getResult() {
        return result;
    }

    public void setResult(List<User> result) {
        this.result = result;
    }
}
