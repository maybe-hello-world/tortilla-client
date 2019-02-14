package kell.guacamole.small.domain;

import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.guacamole.GuacamoleException;

import java.util.Base64;

@Data
public class UserInfoRestModel {

    String domain;
    String serverkey;
    String username;
    @Expose(deserialize = false, serialize = false)
    String userkey;
    @Expose(deserialize = false, serialize = false)
    String password;

    //TODO: check it, is it needed
    @SneakyThrows(GuacamoleException.class)
    public String getPassword() {
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
            password = new String(res);
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
