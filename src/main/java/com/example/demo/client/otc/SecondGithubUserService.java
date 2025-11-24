package com.example.demo.client.otc;

import com.example.demo.model.GithubUser;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/user")
public interface SecondGithubUserService {

    @GetExchange
    GithubUser getAuthenticatedUser();
}
