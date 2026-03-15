/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */



/**
 *
 * @author mac
 */
package com.example.githubaccessreport.client;

import com.example.githubaccessreport.config.GithubConfig;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GithubApiClient {

    private final GithubConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    public GithubApiClient(GithubConfig config) {
        this.config = config;
    }

    private HttpHeaders headers() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + config.getToken());
        headers.set("Accept", "application/vnd.github+json");

        return headers;
    }

    // Organization repositories
    public String getOrganizationRepos(String org) {

        String url = config.getApiUrl() +
                "/orgs/" + org +
                "/repos?per_page=100";

        HttpEntity<String> entity = new HttpEntity<>(headers());

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        return response.getBody();
    }

    // User repositories
    public String getUserRepos(String username) {

        String url = config.getApiUrl() +
                "/users/" + username +
                "/repos?per_page=100";

        HttpEntity<String> entity = new HttpEntity<>(headers());

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        return response.getBody();
    }

    // Repository collaborators
    public String getRepoCollaborators(String owner, String repo) {

        String url = config.getApiUrl() +
                "/repos/" + owner +
                "/" + repo +
                "/collaborators";

        HttpEntity<String> entity = new HttpEntity<>(headers());

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        return response.getBody();
    }

    // Repository contributors (fallback)
    public String getRepoContributors(String owner, String repo) {

        String url = config.getApiUrl() +
                "/repos/" + owner +
                "/" + repo +
                "/contributors";

        HttpEntity<String> entity = new HttpEntity<>(headers());

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        return response.getBody();
    }
}