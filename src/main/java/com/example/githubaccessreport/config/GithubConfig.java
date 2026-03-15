/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.githubaccessreport.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GithubConfig {

    @Value("${github.token}")
    private String token;

    @Value("${github.api.url}")
    private String apiUrl;

    public String getToken() {
        return token;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}