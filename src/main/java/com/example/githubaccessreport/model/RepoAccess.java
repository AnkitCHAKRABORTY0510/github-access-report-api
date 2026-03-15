/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.githubaccessreport.model;

public class RepoAccess {

    private String repository;
    private String permission;

    public RepoAccess(String repository, String permission) {
        this.repository = repository;
        this.permission = permission;
    }

    public String getRepository() {
        return repository;
    }

    public String getPermission() {
        return permission;
    }
}
