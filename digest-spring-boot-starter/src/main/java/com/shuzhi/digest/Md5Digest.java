package com.shuzhi.digest;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author xuyonghong
 * @date 2023-04-05 15:17
 **/
public class Md5Digest implements Digest{
    /**
     * 生成摘要
     *
     * @param raw
     * @return
     */
    @Override
    public String digest(String raw) {
        System.out.println("digest>>>>>>>>> md5 >>>>>>>> " + raw);
        return DigestUtils.md5Hex(raw);
    }
}
