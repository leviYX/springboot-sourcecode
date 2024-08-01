/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.web.servlet;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for multi-part uploads. Adds a
 * {@link StandardServletMultipartResolver} if none is present, and adds a
 * {@link javax.servlet.MultipartConfigElement multipartConfigElement} if none is
 * otherwise defined. The {@link ServletWebServerApplicationContext} will associate the
 * {@link MultipartConfigElement} bean to any {@link Servlet} beans.
 * <p>
 * The {@link javax.servlet.MultipartConfigElement} is a Servlet API that's used to
 * configure how the server handles file uploads.
 *
 * @author Greg Turnquist
 * @author Josh Long
 * @author Toshiaki Maki
 * @since 2.0.0
 */
// lite模式开启
@Configuration(proxyBeanMethods = false)
// 存在这几个类就生效，这都是web包下面的，你引入web这个肯定生效
@ConditionalOnClass({ Servlet.class, StandardServletMultipartResolver.class, MultipartConfigElement.class })
// 当存在这个配置就生效，默认是开启的enabled=true,而且即便没匹配到，也会给你设置为true生效，也就是你配不配，这个也生效，因为本身就提供上传
// 能力，不能因为你不配，我就不解析了，这种是boot的一个规范，
@ConditionalOnProperty(prefix = "spring.servlet.multipart", name = "enabled", matchIfMissing = true)
// 存在这个类就生效，这个类是springboot提供的，springboot提供上传解析器，所以这个类肯定存在，这个类就是用来解析的，
@ConditionalOnWebApplication(type = Type.SERVLET)
// 配置文件绑定，这个EnableConfigurationProperties注解两个作用，一个是把配置文件绑定到MultipartProperties这个类上，
// 另一个是把MultipartProperties这个类注入到容器中,所以关于文件解析的所有可配置项就已经在MultipartProperties指定了，你不配就走默认，
// 配置了就绑定你配置的
@EnableConfigurationProperties(MultipartProperties.class)
public class MultipartAutoConfiguration {

	// 文件配置绑定类
	private final MultipartProperties multipartProperties;

	public MultipartAutoConfiguration(MultipartProperties multipartProperties) {
		this.multipartProperties = multipartProperties;
	}

	@Bean
	@ConditionalOnMissingBean({ MultipartConfigElement.class, CommonsMultipartResolver.class })
	public MultipartConfigElement multipartConfigElement() {
		return this.multipartProperties.createMultipartConfig();
	}

	/**
	 * 注册一个文件解析器，名字为 DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME=multipartResolver
	 * 当容器中没有MultipartResolver这个类的时候，才会注册这个解析器，所以我们自己写了就能覆盖这个，不过一般没必要，因为你按标准servlet
	 * 上传就好了。他就能给你解析了，而且再解析也就是流，你还能玩出什么花来。
	 * 而且看他这个名字StandardServletMultipartResolver，标准servlet类型的上传的文件才能被解析，你要是解析你自己定义的流那种，你得自己写了。boot管不了
	 *
	 */
	@Bean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
	@ConditionalOnMissingBean(MultipartResolver.class)
	public StandardServletMultipartResolver multipartResolver() {
		// 我们看到这里其实给boot放了一个解析器进去，所以文件解析的逻辑就在StandardServletMultipartResolver里面实现
		StandardServletMultipartResolver multipartResolver = new StandardServletMultipartResolver();
		multipartResolver.setResolveLazily(this.multipartProperties.isResolveLazily());
		return multipartResolver;
	}

}
