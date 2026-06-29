package com.lms.user.repository;

import com.lms.user.model.User;
import com.lms.user.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findByUser(User user);
}
