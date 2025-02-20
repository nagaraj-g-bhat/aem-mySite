/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.adobe.aem.mysite.core.servlets;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Servlet that writes some sample content into the response. It is mounted for
 * all resources of a specific Sling resource type. The
 * {@link SlingSafeMethodsServlet} shall be used for HTTP methods that are
 * idempotent. For write operations use the {@link SlingAllMethodsServlet}.
 */
@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes="mysite/components/page",
        methods=HttpConstants.METHOD_GET,
        selectors = "query-builder",
        extensions = "json")
@ServiceDescription("Query Builder Servlet")
public class QueryBuilderServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws ServletException, IOException {
        ResourceResolver resolver = request.getResourceResolver();
        Session session = resolver.adaptTo(Session.class);
        try {

            JsonArray resultArray = new JsonArray();
            Map<String,String> map = new HashMap<>();
            map.put("type", "cq:Page");
            map.put("path", "/content/mysite/us/en");
            map.put("property", "jcr:content/jcr:title");
            map.put("property.value", "Servlet Component");
            map.put("p.limit", "-1");

            QueryBuilder queryBuilder = resolver.adaptTo(QueryBuilder.class);
            Query query = Objects.requireNonNull(queryBuilder).createQuery(PredicateGroup.create(map), session);
            SearchResult result = query.getResult();
            List<Hit> hits = result.getHits();
            for(Hit hit : hits){
                JsonObject pageObject = new JsonObject();
                pageObject.addProperty("path", hit.getPath());
                resultArray.add(pageObject);
            }
            response.setContentType("application/json");
            response.getWriter().write(resultArray.toString());
        } catch (IOException | RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
}
