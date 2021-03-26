package cn.edu.njnu.opengms.userserver.config;

import cn.edu.njnu.opengms.userserver.service.MongoClientDetailsService;
import cn.edu.njnu.opengms.userserver.service.MongoUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServer extends AuthorizationServerConfigurerAdapter {
    @Autowired
    MongoUserDetailsService mongoUserDetails;
    @Autowired
    MongoClientDetailsService mongoClientDetails;
    @Autowired
    AuthenticationManager authenticationManager;

    // jwt-token 配置
    @Autowired
    TokenStore tokenStore;
    @Autowired
    JwtAccessTokenConverter jwtAccessTokenConverter;

    /**
     * 配置 jwt tokenServices
     */
    // AuthorizationServerTokenServices tokenServices(){
    //     TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
    //     // tokenEnhancerChain.setTokenEnhancers();
    //     new JwtTokenStore(new JwtTokenConfig().jwtAccessTokenConverter());
    //     return null;
    // }

    /**
     * MongoDB持久化，两个步骤
     * 1.实现 ClientDetailsService 接口
     * 2.重写 configure(ClientDetailsServiceConfigurer clients)方法
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(mongoClientDetails);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager);
        endpoints.userDetailsService(mongoUserDetails);
        //配置 jwt-token
        endpoints.accessTokenConverter(jwtAccessTokenConverter);
        endpoints.tokenStore(tokenStore);

    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.allowFormAuthenticationForClients();
        security.checkTokenAccess("isAuthenticated()");
    }
}
