package kell.guacamole.small.domain;

import lombok.Data;

@Data
public class VmInfoRestModel {
    private String vmhost;
    private String vmprovider;
    private String protocol;
    private String port;
}
