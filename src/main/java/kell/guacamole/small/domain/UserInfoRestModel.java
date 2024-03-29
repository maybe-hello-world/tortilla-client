package kell.guacamole.small.domain;

import com.google.gson.annotations.Expose;
import lombok.*;
import org.apache.guacamole.GuacamoleException;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


@Setter
@Getter
@EqualsAndHashCode(doNotUseGetters = true)
@RequiredArgsConstructor
public class UserInfoRestModel {
    private Logger logger = LogManager.getLogger(UserInfoRestModel.class);

    String domain;
    String serverkey;
    String username;
    @Expose(deserialize = false, serialize = false)
    String userkey;
    @Expose(deserialize = false, serialize = false)
    String password;

    public String getPassword() throws GuacamoleException {
        if (password == null && serverkey != null && userkey != null) {
            byte[] txt = Base64.getDecoder().decode(userkey);
            byte[] key = Base64.getDecoder().decode(serverkey);
            if (txt.length != key.length) {
                throw new GuacamoleException("Keys length mismatch during password reconstruction." +
                        String.format(" Userkey length: %d, serverkey length: %d.", userkey.length(), serverkey.length()));
            }

            byte[] res = new byte[userkey.length()];
            for (int i = 0; i < txt.length; i++) {
                res[i] = (byte) (txt[i] ^ key[i]);
            }
            try {
                password = new String(res,"windows-1251" );
            } catch (UnsupportedEncodingException e) {
                logger.error("Attention! Passwords can be restored incorrectly! Please install cp1251 charset on the server");
                password = new String(res);
            }
        }
        return password;
    }

    @Override
    public String toString() {
        return "UserInfoRestModel{" +
                "domain='" + domain + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

}
