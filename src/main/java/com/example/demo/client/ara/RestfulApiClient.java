package com.example.demo.client.ara;

import java.util.List;

import com.example.demo.model.ApiObject;
import com.example.demo.model.ApiObjectRequest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

public interface RestfulApiClient {

    @GetExchange("/objects")
    List<ApiObject> getAllObjects();

    @GetExchange("/objects")
    List<ApiObject> getObjectsByIds(@RequestParam("id") List<String> ids);

    @GetExchange("/objects/{id}")
    ApiObject getObjectById(@PathVariable String id);

    @PostExchange("/objects")
    ApiObject createObject(@RequestBody ApiObjectRequest request);

    @PutExchange("/objects/{id}")
    ApiObject updateObject(@PathVariable String id, @RequestBody ApiObjectRequest request);

    @PatchExchange("/objects/{id}")
    ApiObject partialUpdateObject(@PathVariable String id, @RequestBody ApiObjectRequest request);

    @DeleteExchange("/objects/{id}")
    void deleteObject(@PathVariable String id);
}
