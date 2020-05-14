package com.netent.bookstore.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netent.bookstore.model.MediaPost;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class MediaService {

    private List<MediaPost> posts = null;

    @Autowired
    RestTemplate restTemplate;

    private ObjectMapper mapper = new ObjectMapper();

    public List<MediaPost> searchMedia() {
        if (null == posts) {
            try {
                HttpHeaders headers = new HttpHeaders();
                HttpEntity<?> entity = new HttpEntity<>(headers);
                ResponseEntity<List> response = restTemplate.exchange("https://jsonplaceholder.typicode.com/posts", HttpMethod.GET, entity, List.class);
                posts = mapper.convertValue(response.getBody(), new TypeReference<List<MediaPost>>() {
                });
            } catch (Exception e) {
                log.error("Error fetching the media results", e);
                throw new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE," Call to external api resulted in error");
            }
        }
        return posts;
    }

}
