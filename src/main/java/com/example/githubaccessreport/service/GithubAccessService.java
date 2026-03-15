/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.githubaccessreport.service;

import com.example.githubaccessreport.client.GithubApiClient;
import com.example.githubaccessreport.model.RepoAccess;
import com.example.githubaccessreport.model.UserAccess;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

@Service
public class GithubAccessService {

    private final GithubApiClient client;

    public GithubAccessService(GithubApiClient client) {
        this.client = client;
    }

    // ==============================
    // FULL ACCESS REPORT
    // ==============================
    public List<UserAccess> generateAccessReport(String owner) {

        Map<String, List<RepoAccess>> userRepoMap = new ConcurrentHashMap<>();

        try {

            ObjectMapper mapper = new ObjectMapper();
            String reposJson;

            // Try organization first
            try {
                reposJson = client.getOrganizationRepos(owner);
            } catch (Exception e) {
                reposJson = client.getUserRepos(owner);
            }

            if (reposJson == null || reposJson.isEmpty())
                throw new RuntimeException("No repositories found");

            JsonNode repos = mapper.readTree(reposJson);

            // PARALLEL PROCESSING
            StreamSupport.stream(repos.spliterator(), true)
                    .forEach(repo -> {

                        try {

                            String repoName = repo.get("name").asText();

                            String usersJson;

                            try {
                                usersJson = client.getRepoCollaborators(owner, repoName);
                            } catch (Exception e) {
                                usersJson = client.getRepoContributors(owner, repoName);
                            }

                            if (usersJson == null || usersJson.isEmpty())
                                return;

                            JsonNode users = mapper.readTree(usersJson);

                            if (!users.isArray())
                                return;

                            for (JsonNode user : users) {

                                if (!user.has("login"))
                                    continue;

                                String username = user.get("login").asText();

                                String permission = "unknown";

                                if (user.has("permissions")) {

                                    JsonNode perms = user.get("permissions");

                                    if (perms.get("admin").asBoolean())
                                        permission = "admin";
                                    else if (perms.get("push").asBoolean())
                                        permission = "write";
                                    else
                                        permission = "read";
                                }

                                RepoAccess repoAccess =
                                        new RepoAccess(repoName, permission);

                                userRepoMap
                                        .computeIfAbsent(
                                                username,
                                                k -> Collections.synchronizedList(new ArrayList<>())
                                        )
                                        .add(repoAccess);
                            }

                        } catch (Exception ignored) {
                        }
                    });

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate report: " + e.getMessage());
        }

        return convertToUserAccessList(userRepoMap);
    }

    // ==============================
    // PAGINATED ACCESS REPORT
    // ==============================
    public List<UserAccess> generateAccessReportPaged(String owner, int page, int size) {

        Map<String, List<RepoAccess>> userRepoMap = new ConcurrentHashMap<>();

        try {

            ObjectMapper mapper = new ObjectMapper();

            String reposJson;

            try {
                reposJson = client.getOrganizationRepos(owner);
            } catch (Exception e) {
                reposJson = client.getUserRepos(owner);
            }

            if (reposJson == null || reposJson.isEmpty())
                return new ArrayList<>();

            JsonNode repos = mapper.readTree(reposJson);

            int start = (page - 1) * size;
            int end = Math.min(start + size, repos.size());

            if (start >= repos.size())
                return new ArrayList<>();

            for (int i = start; i < end; i++) {

                JsonNode repo = repos.get(i);
                String repoName = repo.get("name").asText();

                String usersJson;

                try {
                    usersJson = client.getRepoCollaborators(owner, repoName);
                } catch (Exception e) {
                    try {
                        usersJson = client.getRepoContributors(owner, repoName);
                    } catch (Exception ex) {
                        continue; // skip repo
                    }
                }

                if (usersJson == null || usersJson.isEmpty())
                    continue;

                JsonNode users;

                try {
                    users = mapper.readTree(usersJson);
                } catch (Exception ex) {
                    continue;
                }

                if (!users.isArray())
                    continue;

                for (JsonNode user : users) {

                    if (!user.has("login"))
                        continue;

                    String username = user.get("login").asText();

                    RepoAccess repoAccess =
                            new RepoAccess(repoName, "unknown");

                    userRepoMap
                            .computeIfAbsent(
                                    username,
                                    k -> new ArrayList<>()
                            )
                            .add(repoAccess);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate paged report: " + e.getMessage());
        }

        return convertToUserAccessList(userRepoMap);
    }

    // ==============================
    // HELPER METHOD
    // ==============================
    private List<UserAccess> convertToUserAccessList(Map<String, List<RepoAccess>> userRepoMap) {

        List<UserAccess> result = new ArrayList<>();

        for (Map.Entry<String, List<RepoAccess>> entry : userRepoMap.entrySet()) {

            result.add(
                    new UserAccess(
                            entry.getKey(),
                            entry.getValue()
                    )
            );
        }

        return result;
    }
}