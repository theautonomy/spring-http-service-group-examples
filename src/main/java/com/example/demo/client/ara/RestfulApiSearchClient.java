package com.example.demo.client.ara;

import java.util.List;

import com.example.demo.model.ApiObject;
import com.example.demo.model.ObjectSearch;

import org.springframework.web.service.annotation.GetExchange;

/**
 * HTTP Service Client demonstrating custom argument resolver with ObjectSearch.
 *
 * <p>This client is manually configured with ObjectSearchArgumentResolver to demonstrate the
 * HttpServiceArgumentResolver pattern.
 */
public interface RestfulApiSearchClient {

    /**
     * Search objects using custom ObjectSearch parameter. The ObjectSearch parameter will be
     * converted to query parameters by ObjectSearchArgumentResolver.
     *
     * @param search search criteria
     * @return list of matching objects
     */
    @GetExchange("/objects")
    List<ApiObject> searchObjects(ObjectSearch search);
}
