/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.githubaccessreport.service;

import com.example.githubaccessreport.client.GithubApiClient;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GithubService {

    private final GithubApiClient client;

    public GithubService(GithubApiClient client) {
        this.client = client;
    }

    public String getRepos(String org) {
        return client.getOrganizationRepos(org);
    }

    public String getAccessReport(String org) {

        try {

            String reposJson = client.getOrganizationRepos(org);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode repos = mapper.readTree(reposJson);

            StringBuilder report = new StringBuilder();

            for (JsonNode repo : repos) {

                String repoName = repo.get("name").asText();

                report.append("Repository: ").append(repoName).append("\n");

                String collaboratorsJson =
                        client.getRepoCollaborators(org, repoName);

                JsonNode collaborators = mapper.readTree(collaboratorsJson);

                for (JsonNode user : collaborators) {
                    report.append("   User: ")
                          .append(user.get("login").asText())
                          .append("\n");
                }

                report.append("\n");
            }

            return report.toString();

        } catch (Exception e) {
            return "Error generating report: " + e.getMessage();
        }
    }
}