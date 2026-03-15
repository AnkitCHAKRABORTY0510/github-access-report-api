/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.githubaccessreport.controller;

import com.example.githubaccessreport.model.UserAccess;
import com.example.githubaccessreport.service.GithubAccessService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github")
public class GithubController {

    private final GithubAccessService service;

    public GithubController(GithubAccessService service) {
        this.service = service;
    }

    @Operation(
            summary = "Generate access report",
            description = "Fetch all repositories for an organization or user and generate user access mapping"
    )
    @GetMapping("/access-report/{owner}")
    public List<UserAccess> getAccessReport(
            @Parameter(description = "GitHub organization or username")
            @PathVariable String owner) {

        return service.generateAccessReport(owner);
    }

    @Operation(
            summary = "Generate paginated access report",
            description = "Fetch repositories using pagination"
    )
    @GetMapping("/access-report/{owner}/paged")
    public List<UserAccess> getAccessReportPaged(

            @Parameter(description = "GitHub organization or username")
            @PathVariable String owner,

            @Parameter(description = "Page number")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "100") int size) {

        return service.generateAccessReportPaged(owner, page, size);
    }

}