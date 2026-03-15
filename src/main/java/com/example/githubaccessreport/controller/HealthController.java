/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.githubaccessreport.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class HealthController {

    @Operation(summary = "Health check endpoint")
    @GetMapping("/")
    public String health() {
        return "GitHub Access Report Service Running 🚀";
    }
}