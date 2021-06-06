package com.veeja.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import com.veeja.annotation.RetExclude;
import com.veeja.entity.Result;
import lombok.extern.slf4j.Slf4j;

/**
 * method环绕处理
 *
 * @author veeja
 */
@Slf4j
public class AroundMethod implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object ret;
        ret = methodInvocation.proceed();
        // 防止出现二次封装
        if (ret instanceof Result) {
            return ret;
        }
        RetExclude re = methodInvocation.getMethod().getAnnotation(RetExclude.class);
        if (null != re) {
            log.info("api添加了封装排除注解");
            return ret;
        }
        return Result.ok(ret);
    }
}