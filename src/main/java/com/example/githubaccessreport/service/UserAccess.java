/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.githubaccessreport.model;

import java.util.List;

public class UserAccess {

    private String username;
    private List<RepoAccess> repositories;

    public UserAccess(String username, List<RepoAccess> repositories) {
        this.username = username;
        this.repositories = repositories;
    }

    public String getUsername() {
        return username;
    }

    public List<RepoAccess> getRepositories() {
        return repositories;
    }
}