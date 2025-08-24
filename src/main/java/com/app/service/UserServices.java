package com.app.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.app.DTOs.SignupRequest;
import com.app.entity.User;
import com.app.exception.UserException;

public interface UserServices {

    User FindUserProfileByJwt(String jwt) throws UserException;

    User save(User user) throws UserException;

    User getUserByid(String id) throws UserException;

    User getUserByEmail(String email) throws UserException;

    User getUserByPhone(String phone) throws UserException;

    public Page<User> getAllUsersWithUserRole(Pageable pageable) throws UserException;

    User update(User user, String id) throws UserException;

    public void CreateUser(SignupRequest request) throws UserException;

    public void delete(String id) throws UserException;

    public boolean verifyOtp(String contact, String submittedOtp);

    public void generateAndSendOtp(String contact) throws UserException;

    List<User> findAllUser() throws UserException;

}
