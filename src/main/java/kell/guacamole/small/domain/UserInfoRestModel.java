package kell.guacamole.small.domain;

import com.google.gson.annotations.Expose;
import lombok.Data;

@Data
public class UserInfoRestModel {

    String domain;
    String serverkey;
    String username;
    @Expose(deserialize = false, serialize = false)
    String userkey;
    @Expose(deserialize = false, serialize = false)
    String password;

    public String getPassword() {
        if (password == null && serverkey != null && userkey != null) {
            byte[] txt = userkey.getBytes();
            byte[] key = serverkey.getBytes();
            byte[] res = new byte[userkey.length()];
            for (int i = 0; i < txt.length; i++) {
                res[i] = (byte) (txt[i] ^ key[i % key.length]);
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
