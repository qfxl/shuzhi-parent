package com.shuzhi.digest;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author xuyonghong
 * @date 2023-04-05 15:15
 **/
public class ShaDigest implements Digest{
    /**
     * 生成摘要
     *
     * @param raw
     * @return
     */
    @Override
    public String digest(String raw) {
        System.out.println("digest>>>>>>>>> sha >>>>>>>> " + raw);
        return DigestUtils.sha256Hex(raw);
    }
}
