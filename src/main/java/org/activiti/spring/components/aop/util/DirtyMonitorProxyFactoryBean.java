package org.activiti.spring.components.aop.util;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * provides notifications whenever a method has had a method invoked which <em>hopefully</em> represents all
 * state change events for the object.
 * <p/>
 * This will <em>not</em> work on direct field accesseso or changes.
 *
 * @author Josh Long
 * @since 5.3
 */
public class DirtyMonitorProxyFactoryBean extends ProxyConfig implements MethodInterceptor, FactoryBean<Object> {

	private ClassLoader beanClassLoader;
	private Object objectToMonitor;
	private ObjectDirtiedListener objectDirtiedListener;

	private Advisor advisor = new Advisor() {
		public Advice getAdvice() {
			return DirtyMonitorProxyFactoryBean.this;
		}

		public boolean isPerInstance() {
			return true;
		}
	};

	public DirtyMonitorProxyFactoryBean(Object o, ObjectDirtiedListener objectDirtiedListener) {
		this.objectToMonitor = o;
		this.objectDirtiedListener = objectDirtiedListener;
		this.beanClassLoader = ClassUtils.getDefaultClassLoader();
		this.setProxyTargetClass(true);
	}

	public Object getObject() throws Exception {
		return createDirtyMonitorProxy(this.objectToMonitor);
	}

	public Class<?> getObjectType() {
		return this.objectToMonitor.getClass();
	}

	public boolean isSingleton() {
		return false;
	}

	/**
	 * clients of this class must implement this listener to be notified of when an object has changed.
	 */
	public static interface ObjectDirtiedListener {
		void onMethodInvoked(Object o, Method method);
	}

	private Object createDirtyMonitorProxy(Object bean) {

		Class<?> targetClass = AopUtils.getTargetClass(bean);
		if (AopUtils.canApply(this.advisor, targetClass)) {// always true since we have no pointcut
			if (bean instanceof Advised) {
				((Advised) bean).addAdvisor(0, this.advisor);
				return bean;
			} else {
				ProxyFactory proxyFactory = new ProxyFactory(bean);
				proxyFactory.copyFrom(this);
				proxyFactory.addAdvisor(this.advisor);
				return proxyFactory.getProxy(this.beanClassLoader);
			}
		} else {
			return bean;
		}
	}

	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Object result = methodInvocation.proceed();
		this.objectDirtiedListener.onMethodInvoked(this.objectToMonitor, methodInvocation.getMethod());
		return result;
	}
}
