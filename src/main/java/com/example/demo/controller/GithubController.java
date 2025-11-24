package com.example.demo.controller;

import com.example.demo.client.github.GithubUserService;
import com.example.demo.client.otc.SecondGithubUserService;
import com.example.demo.model.GithubUser;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/github")
public class GithubController {

    private final GithubUserService githubUserService;
    private final SecondGithubUserService secondGithubUserService;

    public GithubController(
            GithubUserService githubUserService, SecondGithubUserService secondGithubUserService) {
        this.githubUserService = githubUserService;
        this.secondGithubUserService = secondGithubUserService;
    }

    @GetMapping("/user")
    public GithubUser getAuthenticatedUser() {
        return githubUserService.getAuthenticatedUser();
    }

    @GetMapping("/second-user")
    public GithubUser getSecondAuthenticatedUser() {
        return secondGithubUserService.getAuthenticatedUser();
    }
}
