package cn.edu.njnu.opengms.userserver.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "oauth2ClientsDetails")
public class ClientDetails {
    private String clientId;
    private String clientSecret;
    private String resourceIds;
    private String scope;
    private String authorizedGrantTypes;
    private String registeredRedirectUris;
    private Integer accessTokenValiditySeconds;
    private Integer refreshTokenValiditySeconds;
    private String additionalInformation;
    private String authorities;
    //设置用户是否自动 Approval 操作，默认为false，包括true,false,read,write.
    private String autoapprove;
    private String autoApproveScopes;
    //是否支持refresh_token
    private boolean supportRefreshToken;
}
