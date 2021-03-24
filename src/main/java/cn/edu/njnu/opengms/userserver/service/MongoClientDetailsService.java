package cn.edu.njnu.opengms.userserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;


@Service
public class MongoClientDetailsService implements ClientDetailsService {
    @Autowired
    MongoTemplate mongoTemplate;
    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        Query query = new Query(Criteria.where("clientId").is(clientId));
        BaseClientDetails client = mongoTemplate.findOne(query, BaseClientDetails.class, "oauth2ClientsDetails");
        return client;
    }
}
