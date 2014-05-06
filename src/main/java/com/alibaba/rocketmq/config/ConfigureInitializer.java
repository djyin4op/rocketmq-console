package com.alibaba.rocketmq.config;


import com.alibaba.rocketmq.common.MixAll;
import org.apache.commons.lang.StringUtils;


/**
 * 把需要补充的初始化环境变量正式的放入系统属性中
 *
 * @author yankai913@gmail.com
 * @date 2014-2-10
 */
public class ConfigureInitializer {

    private String namesrvAddr;

    public String getNamesrvAddr() {
        return namesrvAddr;
    }

    public void setNamesrvAddr(String namesrvAddr) {
        this.namesrvAddr = namesrvAddr;
    }

    public void init() {
        // 采用rocketmq默认的查找逻辑
        if (StringUtils.isNotBlank(this.namesrvAddr)) {
            System.setProperty(MixAll.NAMESRV_ADDR_PROPERTY, namesrvAddr);
        }

    }
}
