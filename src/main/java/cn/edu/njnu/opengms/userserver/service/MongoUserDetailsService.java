package cn.edu.njnu.opengms.userserver.service;

import cn.edu.njnu.opengms.userserver.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Component
public class MongoUserDetailsService implements UserDetailsService {
    private final MongoTemplate mongoTemplate;
    public MongoUserDetailsService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * spring security 的验证方法
     * 根据 email 查询数据并将其返回
     * @param email
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Query query = new Query(Criteria.where("email").is(email));
        User user = mongoTemplate.findOne(query, User.class, "users");
        if (user != null){
            return buildUserForAuthentication(user, Arrays.asList(new SimpleGrantedAuthority("user")));
        }else {
            throw new UsernameNotFoundException("Email not found.");
        }
    }

    /**
     * 将数据库中 user转换为可被 security 识别的用户类型
     * username: email
     * password: password
     * @param user
     * @param authorities
     * @return
     */
    private UserDetails buildUserForAuthentication(User user, List<GrantedAuthority> authorities){
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }
}
