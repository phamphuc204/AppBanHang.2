package com.hoan.appbanhang.model;

import java.util.List;

public class SanPhamMoiModel {
    boolean success;
    String message;
    List<SanPhamMoi> result;

    public boolean isSucess() {
        return success;
    }

    public void setSucess(boolean sucess) {
        this.success = sucess;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<SanPhamMoi> getResult() {
        return result;
    }

    public void setResult(List<SanPhamMoi> result) {
        this.result = result;
    }
}
