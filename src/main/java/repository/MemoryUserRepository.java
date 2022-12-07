package repository;

import model.User;

import java.util.HashMap;


public class MemoryUserRepository {
    private static HashMap<String, User> store = new HashMap<String, User>();
}
