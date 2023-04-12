package com.shuzhi.digest;

/**
 * @author xuyonghong
 * @date 2023-04-05 15:10
 **/
public interface Digest {
    /**
     * 生成摘要
     * @param raw
     * @return
     */
    String digest(String raw);
}
