package com.employment.common;

/**
 * 业务异常：携带可直接展示给用户的中文提示。
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }
}
