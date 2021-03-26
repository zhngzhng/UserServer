package cn.edu.njnu.opengms.userserver.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;

/**
 * 废弃，用户服务器不对密码进行加密操作
 */
public class Md5PasswordEncoder implements PasswordEncoder {
    private final Log logger = LogFactory.getLog(getClass());

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("password can't be null.");
        }
        return DigestUtils.md5DigestAsHex(rawPassword.toString().getBytes());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }

        if (encodedPassword == null || encodedPassword.length() == 0) {
            logger.warn("Empty encoded password");
            return false;
        }
        return DigestUtils.md5DigestAsHex(rawPassword.toString().getBytes()).equals(encodedPassword);
    }
}
