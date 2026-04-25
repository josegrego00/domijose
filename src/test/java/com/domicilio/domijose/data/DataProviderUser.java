package com.domicilio.domijose.data;

import com.domicilio.domijose.dto.UserDTO;
import com.domicilio.domijose.models.User;
import com.domicilio.domijose.models.enums.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * Data provider for User test data
 */
public class DataProviderUser {

    public static User createUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPhone("1234567890");
        user.setRole(Role.CLIENTE);
        return user;
    }

    public static User createUserWithRole(Role role) {
        User user = createUser();
        user.setRole(role);
        return user;
    }

    public static UserDTO createUserDTO() {
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setEmail("test@example.com");
        dto.setFullName("Test User");
        dto.setPhone("1234567890");
        dto.setPassword("password123");
        return dto;
    }

    public static UserDTO createUserDTOWithoutPassword() {
        UserDTO dto = createUserDTO();
        dto.setPassword(null);
        return dto;
    }

    public static List<User> createUserList() {
        List<User> users = new ArrayList<>();
        users.add(createUser());
        
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("admin@example.com");
        user2.setFullName("Admin User");
        user2.setPhone("0987654321");
        user2.setRole(Role.ADMIN);
        users.add(user2);
        
        return users;
    }

    public static User createUserWithEmail(String email) {
        User user = createUser();
        user.setEmail(email);
        return user;
    }

    public static User createUserWithPhone(String phone) {
        User user = createUser();
        user.setPhone(phone);
        return user;
    }
}