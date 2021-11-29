package com.clickbait.plugin.transformer;

import com.clickbait.plugin.config.RssConfig;
import com.rometools.rome.feed.synd.SyndEntry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.transformer.AbstractPayloadTransformer;
import org.springframework.stereotype.Component;

@Component
public class RssTransformer {
    @Autowired
    private RssConfig config;

    @Bean(name = "extractLinkFromFeed")
    public AbstractPayloadTransformer<SyndEntry, String> extractLinkFromFeed() {
        return new AbstractPayloadTransformer<SyndEntry, String>() {
            @Override
            protected String transformPayload(SyndEntry payload) {
                return String.format(config.getTransform(), payload.getTitle(), payload.getAuthor(), payload.getLink())
                        + System.lineSeparator();
            }
        };
    }
}