package com.veeja.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.veeja.annotation.RetExclude;
import com.veeja.entity.Result;

import lombok.extern.slf4j.Slf4j;  


/**
 * method环绕处理
 * @author veeja
 * @date 2019-08-20
 */
@Slf4j
public class AroundMethod implements MethodInterceptor{  

	@Override  
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {  

		Object ret = null;  
		try {
			ret = methodInvocation.proceed();  
			if(ret instanceof Result) {	// 防止出现二次封装
				return ret;
			}
		} catch (Throwable e) {
			// 运行时出现异常，抛出，由ResultReturnExceptionHandler统一处理
			throw e;
		}  

		RetExclude re = methodInvocation.getMethod().getAnnotation(RetExclude.class);
		if(null != re && null != re.value()) {
			log.info("api添加了封装排除注解");
			return ret;
		}
		// log.info("封装返回值");
		return Result.ok(ret);
	}  
}  