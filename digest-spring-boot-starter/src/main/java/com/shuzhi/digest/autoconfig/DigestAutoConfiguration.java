package com.shuzhi.digest.autoconfig;

import com.shuzhi.digest.Digest;
import com.shuzhi.digest.DigestSettings;
import com.shuzhi.digest.Md5Digest;
import com.shuzhi.digest.ShaDigest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xuyonghong
 * @date 2023-04-05 15:21
 **/
@Configuration
@EnableConfigurationProperties(DigestSettings.class)
public class DigestAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "digest", name = "type", havingValue = "md5")
    public Digest md5Digest() {
        System.out.println(">>>>>>> md5 初始化 >>>>>>>");
        return new Md5Digest();
    }

    @Bean
    @ConditionalOnProperty(prefix = "digest", name = "type", havingValue = "sha")
    public Digest shaDigest() {
        System.out.println(">>>>>>> sha 初始化 >>>>>>>");
        return new ShaDigest();
    }
}
