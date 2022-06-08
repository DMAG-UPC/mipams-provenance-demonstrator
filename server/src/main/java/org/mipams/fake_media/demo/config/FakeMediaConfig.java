package org.mipams.fake_media.demo.config;

import org.mipams.fake_media.services.AssertionFactory;
import org.mipams.fake_media.services.ManifestDiscovery;
import org.mipams.fake_media.services.RedactionService;
import org.mipams.fake_media.services.UriReferenceService;
import org.mipams.fake_media.services.consumer.AssertionStoreConsumer;
import org.mipams.fake_media.services.consumer.ClaimConsumer;
import org.mipams.fake_media.services.consumer.ClaimSignatureConsumer;
import org.mipams.fake_media.services.consumer.ManifestConsumer;
import org.mipams.fake_media.services.consumer.ManifestStoreConsumer;
import org.mipams.fake_media.services.content_types.AssertionStoreContentType;
import org.mipams.fake_media.services.content_types.ClaimContentType;
import org.mipams.fake_media.services.content_types.ClaimSignatureContentType;
import org.mipams.fake_media.services.content_types.CredentialStoreContentType;
import org.mipams.fake_media.services.content_types.ManifestStoreContentType;
import org.mipams.fake_media.services.content_types.StandardManifestContentType;
import org.mipams.fake_media.services.content_types.UpdateManifestContentType;
import org.mipams.fake_media.services.producer.AssertionRefProducer;
import org.mipams.fake_media.services.producer.AssertionStoreProducer;
import org.mipams.fake_media.services.producer.ClaimProducer;
import org.mipams.fake_media.services.producer.ClaimSignatureProducer;
import org.mipams.fake_media.services.producer.ManifestProducer;
import org.mipams.fake_media.services.producer.ManifestStoreProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FakeMediaConfig {

    @Bean
    public AssertionFactory assertionFactory() {
        return new AssertionFactory();
    }

    @Bean
    public AssertionStoreConsumer assertionStoreConsumer() {
        return new AssertionStoreConsumer();
    }

    @Bean
    public ClaimConsumer claimConsumer() {
        return new ClaimConsumer();
    }

    @Bean
    public ManifestConsumer manifestConsumer() {
        return new ManifestConsumer();
    }

    @Bean
    public ClaimSignatureConsumer claimSignatureConsumer() {
        return new ClaimSignatureConsumer();
    }

    @Bean
    public AssertionStoreContentType assertionStoreContentType() {
        return new AssertionStoreContentType();
    }

    @Bean
    public ClaimContentType claimContentType() {
        return new ClaimContentType();
    }

    @Bean
    public ClaimSignatureContentType claimSignatureContentType() {
        return new ClaimSignatureContentType();
    }

    @Bean
    public CredentialStoreContentType credentialStoreContentType() {
        return new CredentialStoreContentType();
    }

    @Bean
    public ManifestStoreContentType manifestContentType() {
        return new ManifestStoreContentType();
    }

    @Bean
    public StandardManifestContentType standardManifestContentType() {
        return new StandardManifestContentType();
    }

    @Bean
    public UpdateManifestContentType updateManifestContentType() {
        return new UpdateManifestContentType();
    }

    @Bean
    public AssertionRefProducer assertionRefProducer() {
        return new AssertionRefProducer();
    }

    @Bean
    public AssertionStoreProducer assertionStoreProducer() {
        return new AssertionStoreProducer();
    }

    @Bean
    public ClaimProducer claimProducer() {
        return new ClaimProducer();
    }

    @Bean
    public ClaimSignatureProducer claimSignatureProducer() {
        return new ClaimSignatureProducer();
    }

    @Bean
    public ManifestDiscovery manifestDiscovery() {
        return new ManifestDiscovery();
    }

    @Bean
    public ManifestStoreConsumer manifestStoreConsumer() {
        return new ManifestStoreConsumer();
    }

    @Bean
    public ManifestStoreProducer manifestStoreProducer() {
        return new ManifestStoreProducer();
    }

    @Bean
    public ManifestProducer manifestProducer() {
        return new ManifestProducer();
    }

    @Bean
    public RedactionService redactionService() {
        return new RedactionService();
    }

    @Bean
    public UriReferenceService uriReferenceService() {
        return new UriReferenceService();
    }

}
