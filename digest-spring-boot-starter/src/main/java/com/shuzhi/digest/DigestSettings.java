package com.shuzhi.digest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author xuyonghong
 * @date 2023-04-05 15:17
 **/
@Component
@ConfigurationProperties(prefix = "digest")
public class DigestSettings {

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
