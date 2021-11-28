package com.clickbait.plugin.config;

import com.clickbait.plugin.ServiceActivators;
import com.clickbait.plugin.Transformers;
import com.clickbait.plugin.pojo.RssConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.feed.dsl.Feed;
import org.springframework.integration.feed.dsl.FeedEntryMessageSourceSpec;
import org.springframework.integration.metadata.MetadataStore;
import org.springframework.integration.metadata.PropertiesPersistingMetadataStore;
import org.springframework.integration.scheduling.PollerMetadata;

@Configuration
public class FlowConfig {

    @Autowired
    private RssConfig config;

    @Autowired
    private ServiceActivators activators;

    @Autowired
    private Transformers transformer;

    @Bean
    public MetadataStore metadataStore() {
        PropertiesPersistingMetadataStore metadataStore = new PropertiesPersistingMetadataStore();
        metadataStore.setBaseDirectory(config.getMetadataFolder());
        return metadataStore;
    }

    @Bean(name = "feedMessageSource")
    public FeedEntryMessageSourceSpec feedMessageSource() {
        return Feed.inboundAdapter(config.getFeed(), config.getTopic()).metadataStore(metadataStore());
    }

    @Bean
    public IntegrationFlow transformFeed() {
        return IntegrationFlows
                .from("transform")
                .transform(transformer.extractLinkFromFeed())
                .channel("storeAndPrint")
                .get();
    }

    @Bean
    public IntegrationFlow rssFeed(@Qualifier("rssPoller") PollerMetadata pollerMetadata) {
        System.out.println();
        return IntegrationFlows
                .from(feedMessageSource(), e -> e.poller(pollerMetadata))
                .fixedSubscriberChannel()
                .channel("transform")
                .get();
    }

    @Bean
    public IntegrationFlow fromStoreAndPrint() {
        return IntegrationFlows
                .from("storeAndPrint")
                .routeToRecipients(r -> r
                        .recipient("print")
                        .recipient("store"))
                .get();
    }

    @Bean
    public IntegrationFlow messageHandlerFlow() {
        return IntegrationFlows
                .from("print")
                .handle(activators.printHandler())
                .get();
    }

    @Bean
    public IntegrationFlow storeHandlerFlow() {
        return IntegrationFlows
                .from("store")
                .handle(activators.storeHandler())
                .get();
    }
}