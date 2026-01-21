package com.example.smartmedicine.base;

/**
 * 统一API响应实体（标准化接口返回格式）
 * @param <T> 响应数据泛型
 */
public class BaseResponse<T> {
    private int code;       // 响应状态码（200=成功，其他为错误）
    private String msg;     // 响应提示信息
    private T data;         // 响应核心数据

    // Getter & Setter（小驼峰命名规范）
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    // 快捷判断响应是否成功
    public boolean isSuccess() {
        return code == 200;
    }
}