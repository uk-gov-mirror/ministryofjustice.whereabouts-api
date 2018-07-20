package uk.gov.justice.digital.hmpps.prisonstaffhub.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import uk.gov.justice.digital.hmpps.prisonstaffhub.utils.*;

import java.util.Arrays;
import java.util.List;

@Configuration
public class RestTemplateConfiguration {

    private final OAuth2ClientContext oauth2ClientContext;
    private final ClientCredentialsResourceDetails elite2apiDetails;
    private final ApiGatewayTokenGenerator apiGatewayTokenGenerator;

    @Value("${elite2.uri.root}")
    private String elit2UriRoot;

    @Value("${elite2.api.uri.root}")
    private String apiRootUri;

    @Value("${use.api.gateway.auth}")
    private boolean useApiGateway;

    @Autowired
    public RestTemplateConfiguration(
            OAuth2ClientContext oauth2ClientContext,
            ClientCredentialsResourceDetails elite2apiDetails,
            ApiGatewayTokenGenerator apiGatewayTokenGenerator) {
        this.oauth2ClientContext = oauth2ClientContext;
        this.elite2apiDetails = elite2apiDetails;
        this.apiGatewayTokenGenerator = apiGatewayTokenGenerator;
    }

    @Bean(name = "elite2ApiRestTemplate")
    public RestTemplate elite2ApiRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return getRestTemplate(restTemplateBuilder, apiRootUri);
    }

    @Bean(name = "elite2ApiHealthRestTemplate")
    public RestTemplate elite2ApiHealthRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return getRestTemplate(restTemplateBuilder, elit2UriRoot);
    }

    private RestTemplate getRestTemplate(RestTemplateBuilder restTemplateBuilder, String uri) {
        return restTemplateBuilder
                .rootUri(uri)
                .additionalInterceptors(getRequestInterceptors())
                .build();
    }

    private List<ClientHttpRequestInterceptor> getRequestInterceptors() {
        if (useApiGateway) {
            return Arrays.asList(
                    new UserContextInterceptor(),
                    new ApiGatewayInterceptor(apiGatewayTokenGenerator));
        } else {
            return Arrays.asList(
                    new UserContextInterceptor(),
                    new JwtAuthInterceptor());
        }
    }

    @Bean
    public OAuth2RestTemplate elite2SystemRestTemplate(GatewayAwareAccessTokenProvider accessTokenProvider) {

        OAuth2RestTemplate elite2SystemRestTemplate = new OAuth2RestTemplate(elite2apiDetails, oauth2ClientContext);
        List<ClientHttpRequestInterceptor> systemInterceptors = elite2SystemRestTemplate.getInterceptors();
        systemInterceptors.add(new UserContextInterceptor());
        if (useApiGateway) {
            systemInterceptors.add(new ApiGatewayBatchRequestInterceptor(apiGatewayTokenGenerator));
            // The access token provider's rest template also needs to know how to get through the gateway
            List<ClientHttpRequestInterceptor> tokenProviderInterceptors = ((RestTemplate) accessTokenProvider.getRestTemplate()).getInterceptors();
            tokenProviderInterceptors.add(new ApiGatewayBatchRequestInterceptor(apiGatewayTokenGenerator));
        } else {
            systemInterceptors.add(new JwtAuthInterceptor());
        }

        elite2SystemRestTemplate.setAccessTokenProvider(accessTokenProvider);

        RootUriTemplateHandler.addTo(elite2SystemRestTemplate, this.apiRootUri);
        return elite2SystemRestTemplate;
    }

    /**
     * This subclass is necessary to make OAuth2AccessTokenSupport.getRestTemplate() public
     */
    @Component("accessTokenProvider")
    public class GatewayAwareAccessTokenProvider extends ClientCredentialsAccessTokenProvider {
        @Override
        public RestOperations getRestTemplate() {
            return super.getRestTemplate();
        }
    }
}
