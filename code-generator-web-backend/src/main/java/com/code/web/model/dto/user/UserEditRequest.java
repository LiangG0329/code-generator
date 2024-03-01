package com.code.web.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Liang
 * @create 2024/2/29
 */
@Data
public class UserEditRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    private static final long serialVersionUID = 1L;
}
