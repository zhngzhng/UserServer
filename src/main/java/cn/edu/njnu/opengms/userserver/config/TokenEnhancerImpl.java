package cn.edu.njnu.opengms.userserver.config;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;

/**
 * @ClassName TokenEnhancerImpl
 * @Description 添加令牌生成时间信息
 * @Author zhngzhng
 * @Date 2021/3/30
 **/
@Service
public class TokenEnhancerImpl implements TokenEnhancer {
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        Date expiration = accessToken.getExpiration();
        HashMap<String, Object> additionInfoMap = new HashMap<>();
        additionInfoMap.put("invalidTime", expiration);
        ((DefaultOAuth2AccessToken)accessToken).setAdditionalInformation(additionInfoMap);
        return accessToken;
    }
}
