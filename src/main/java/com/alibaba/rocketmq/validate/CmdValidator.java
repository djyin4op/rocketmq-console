package com.alibaba.rocketmq.validate;

import com.alibaba.rocketmq.tools.command.MQAdminStartup;
import com.alibaba.rocketmq.tools.command.SubCommand;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author yankai913@gmail.com
 * @date 2014-2-25
 */
public class CmdValidator extends MQAdminStartup implements BeanPostProcessor,
        ApplicationListener<ContextRefreshedEvent> {

    static final Logger logger = LoggerFactory.getLogger(CmdValidator.class);

    private final AtomicBoolean hasChecked = new AtomicBoolean(false);



    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    Map<String, Class<? extends SubCommand>> method2cmd = Maps.newHashMap();
    Map<Class<? extends SubCommand>, String> cmd2method = Maps.newHashMap();


    // 记录已经实现的rocketmq-tools中的命令
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        if (clazz.isAnnotationPresent(Service.class)) {
            Method[] methodArr = clazz.getDeclaredMethods();
            for (Method method : methodArr) {
                if (method.isAnnotationPresent(CmdTrace.class)) {
                    CmdTrace cmdTrace = method.getAnnotation(CmdTrace.class);
                    Class<? extends SubCommand> cmdClazz = cmdTrace.cmdClazz();
                    String methodName = clazz.getSimpleName() + "." + method.getName();
                    if (method2cmd.get(methodName) == null) {
                        method2cmd.put(methodName, cmdClazz);
                    } else {
                        throw new IllegalStateException(methodName + " = {"
                                + method2cmd.get(methodName).getName() + "," + cmdClazz.getName() + "}");
                    }
                    if (cmd2method.get(cmdClazz) == null) {
                        cmd2method.put(cmdClazz, methodName);
                    } else {
                        throw new IllegalStateException(cmdClazz + " = {" + cmd2method.get(cmdClazz) + ","
                                + methodName + "}");
                    }
                }
            }
        }
        return bean;
    }


    // 记录未实现的rocketmq-tools中的命令
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (hasChecked.compareAndSet(false, true)) {
            for (SubCommand cmd : MQAdminStartup.subCommandList) {
                if (cmd2method.containsKey(cmd.getClass())) {
                    logger.info("cmdClazz:{}, method:{}", cmd.getClass().getName(),
                            cmd2method.get(cmd.getClass()));
                } else {
                    // 为实现的rocketmq-tools中的命令，不应该出现异常，应该只记录错误。。。
                    logger.error("cmdClazz:{}, method not found", cmd.getClass().getName());
                }
            }
        }

    }
}
