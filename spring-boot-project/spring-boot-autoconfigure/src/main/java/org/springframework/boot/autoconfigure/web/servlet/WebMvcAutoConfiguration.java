/*
 * Copyright 2012-2020 the original author or authors.
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

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import javax.servlet.Servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.template.TemplateAvailabilityProviders;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidatorAdapter;
import org.springframework.boot.autoconfigure.web.ConditionalOnEnabledResourceChain;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties.Strategy;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.boot.autoconfigure.web.format.WebConversionService;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties.Format;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.filter.OrderedFormContentFilter;
import org.springframework.boot.web.servlet.filter.OrderedHiddenHttpMethodFilter;
import org.springframework.boot.web.servlet.filter.OrderedRequestContextFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.ClassUtils;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.PathExtensionContentNegotiationStrategy;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.FormContentFilter;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceChainRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.resource.AppCacheManifestTransformer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.util.UrlPathHelper;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link EnableWebMvc Web MVC}.
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Andy Wilkinson
 * @author Sébastien Deleuze
 * @author Eddú Meléndez
 * @author Stephane Nicoll
 * @author Kristine Jetzke
 * @author Bruce Brouwer
 * @author Artsiom Yudovin
 * @since 2.0.0
 * 和springmvc相关的自动配置功能
 */
// lite模式启动
@Configuration(proxyBeanMethods = false)
// 当你是个web servlet的时候生效，我们目前导入的就是web，所以生效
@ConditionalOnWebApplication(type = Type.SERVLET)
// 当存在mvc环境时生效，下面三个类都是web的自然也就有mvc，所以生效
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class })
// 当不存在WebMvcConfigurationSupport的时候生效，这就是全面自定义接管mvc环境的，这里我们先不定义，所以生效
@ConditionalOnMissingBean(WebMvcConfigurationSupport.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
// 在DispatcherServletAutoConfiguration TaskExecutionAutoConfiguration  ValidationAutoConfiguration装配之后生效，因为有依赖
// 得先有这三个
@AutoConfigureAfter({ DispatcherServletAutoConfiguration.class, TaskExecutionAutoConfiguration.class,
		ValidationAutoConfiguration.class })
public class WebMvcAutoConfiguration {

	public static final String DEFAULT_PREFIX = "";

	public static final String DEFAULT_SUFFIX = "";

	private static final String[] SERVLET_LOCATIONS = { "/" };

	/**
	 * 这个是注册你对于restful风格请求的映射，当然默认是GET，但是你不配delete put是不能生效的，他会映射去你相同名字的get请求
	 * 没有相同名字的get就404了  注意这个是表单提交的rest风格过滤器，你要是用postman这个不生效，因为表单只能写get post
	 * 不能写delete 和 put，这里你要是原生的其实没事，这里只是为了兼容表单的
	 * @return
	 */
	@Bean
	// 没这个才生效这个配置，我们没自己写，所以生效
	@ConditionalOnMissingBean(HiddenHttpMethodFilter.class)
	// 配置文件里面没有配置spring.mvc.hiddenmethod.filter.enabled=true，所以不生效，所以我们要想生效rest风格，需要配置这个
	@ConditionalOnProperty(prefix = "spring.mvc.hiddenmethod.filter", name = "enabled", matchIfMissing = false)
	public OrderedHiddenHttpMethodFilter hiddenHttpMethodFilter() {
		// 点进去看他的过滤规则
		/**
		 * if ("POST".equals(request.getMethod()) && request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE) == null) {
		 * 			String paramValue = request.getParameter(this.methodParam);
		 * 			if (StringUtils.hasLength(paramValue)) {
		 * 				String method = paramValue.toUpperCase(Locale.ENGLISH);
		 * 				if (ALLOWED_METHODS.contains(method)) {
		 * 					requestToUse = new HttpMethodRequestWrapper(request, method);
		 * 				                }            * 			}
		 * 		}
		 * 	filterChain.doFilter(requestToUse, response);放行过滤器
		 * 	这是他过滤器的实现，我们看到你的方法必须是post，并且没有异常，然后参数里面必须有一个_method参数，这个参数的值就是你的请求方法
		 * 	也就是你的表单要写一个name="_method" value="delete"然后他会读这个_method"，ALLOWED_METHODS这个是个数组就是put get delete post
		 * 	那些支持的rest类型的接口，这里拿到再去给你路由，过滤器给你把你原来的get请求拿到，然后给你包装成了delete请求，然后你就可以处理delete请求了
		 */
		return new OrderedHiddenHttpMethodFilter();
	}

	@Bean
	@ConditionalOnMissingBean(FormContentFilter.class)
	@ConditionalOnProperty(prefix = "spring.mvc.formcontent.filter", name = "enabled", matchIfMissing = true)
	public OrderedFormContentFilter formContentFilter() {
		return new OrderedFormContentFilter();
	}

	static String[] getResourceLocations(String[] staticLocations) {
		String[] locations = new String[staticLocations.length + SERVLET_LOCATIONS.length];
		System.arraycopy(staticLocations, 0, locations, 0, staticLocations.length);
		System.arraycopy(SERVLET_LOCATIONS, 0, locations, staticLocations.length, SERVLET_LOCATIONS.length);
		return locations;
	}

	// Defined as a nested config to ensure WebMvcConfigurer is not read when not
	// on the classpath
	// 处理mvc的配置，比如视图解析器，静态资源处理等，这里就是静态资源的配置生效类
	@Configuration(proxyBeanMethods = false)
	@Import(EnableWebMvcConfiguration.class)
	// 自动注入和文件绑定的类，比如：WebMvcProperties, ResourceProperties，同时把这个绑定的类也放到容器中
	// WebMvcProperties是mvc的配置类和配置文件的spring.mvc绑定，ResourceProperties是静态资源的配置类和配置文件的spring.resources绑定
	@EnableConfigurationProperties({ WebMvcProperties.class, ResourceProperties.class })
	@Order(0)
	public static class WebMvcAutoConfigurationAdapter implements WebMvcConfigurer {

		private static final Log logger = LogFactory.getLog(WebMvcConfigurer.class);

		private final ResourceProperties resourceProperties;

		private final WebMvcProperties mvcProperties;

		private final ListableBeanFactory beanFactory;

		private final ObjectProvider<HttpMessageConverters> messageConvertersProvider;

		private final ObjectProvider<DispatcherServletPath> dispatcherServletPath;

		private final ObjectProvider<ServletRegistrationBean<?>> servletRegistrations;

		final ResourceHandlerRegistrationCustomizer resourceHandlerRegistrationCustomizer;

		// 这里说一个机制，就是当一个配置类(被@Configuration标记的类)，只有一个全参构造的时候，这个构造器里面所有的参数都会从容器中获取
		public WebMvcAutoConfigurationAdapter(ResourceProperties resourceProperties, WebMvcProperties mvcProperties,
				ListableBeanFactory beanFactory, ObjectProvider<HttpMessageConverters> messageConvertersProvider,
				ObjectProvider<ResourceHandlerRegistrationCustomizer> resourceHandlerRegistrationCustomizerProvider,
				ObjectProvider<DispatcherServletPath> dispatcherServletPath,
				ObjectProvider<ServletRegistrationBean<?>> servletRegistrations) {
			//  WebMvcProperties是mvc的配置类和配置文件的spring.mvc绑定，ResourceProperties是静态资源的配置类和配置文件的spring.resources绑定
			this.resourceProperties = resourceProperties;
			this.mvcProperties = mvcProperties;
			this.beanFactory = beanFactory;// 容器工厂
			this.messageConvertersProvider = messageConvertersProvider;// 类型属性转换
			this.resourceHandlerRegistrationCustomizer = resourceHandlerRegistrationCustomizerProvider.getIfAvailable();
			this.dispatcherServletPath = dispatcherServletPath;
			this.servletRegistrations = servletRegistrations;
		}

		@Override
		public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
			this.messageConvertersProvider
					.ifAvailable((customConverters) -> converters.addAll(customConverters.getConverters()));
		}

		@Override
		public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
			if (this.beanFactory.containsBean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)) {
				Object taskExecutor = this.beanFactory
						.getBean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME);
				if (taskExecutor instanceof AsyncTaskExecutor) {
					configurer.setTaskExecutor(((AsyncTaskExecutor) taskExecutor));
				}
			}
			Duration timeout = this.mvcProperties.getAsync().getRequestTimeout();
			if (timeout != null) {
				configurer.setDefaultTimeout(timeout.toMillis());
			}
		}

		@Override
		@SuppressWarnings("deprecation")
		public void configurePathMatch(PathMatchConfigurer configurer) {
			configurer.setUseSuffixPatternMatch(this.mvcProperties.getPathmatch().isUseSuffixPattern());
			configurer.setUseRegisteredSuffixPatternMatch(
					this.mvcProperties.getPathmatch().isUseRegisteredSuffixPattern());
			this.dispatcherServletPath.ifAvailable((dispatcherPath) -> {
				String servletUrlMapping = dispatcherPath.getServletUrlMapping();
				if (servletUrlMapping.equals("/") && singleDispatcherServlet()) {
					UrlPathHelper urlPathHelper = new UrlPathHelper();
					urlPathHelper.setAlwaysUseFullPath(true);
					configurer.setUrlPathHelper(urlPathHelper);
				}
			});
		}

		private boolean singleDispatcherServlet() {
			return this.servletRegistrations.stream().map(ServletRegistrationBean::getServlet)
					.filter(DispatcherServlet.class::isInstance).count() == 1;
		}

		@Override
		@SuppressWarnings("deprecation")
		public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
			WebMvcProperties.Contentnegotiation contentnegotiation = this.mvcProperties.getContentnegotiation();
			configurer.favorPathExtension(contentnegotiation.isFavorPathExtension());
			configurer.favorParameter(contentnegotiation.isFavorParameter());
			if (contentnegotiation.getParameterName() != null) {
				configurer.parameterName(contentnegotiation.getParameterName());
			}
			Map<String, MediaType> mediaTypes = this.mvcProperties.getContentnegotiation().getMediaTypes();
			mediaTypes.forEach(configurer::mediaType);
		}

		@Bean
		@ConditionalOnMissingBean
		public InternalResourceViewResolver defaultViewResolver() {
			InternalResourceViewResolver resolver = new InternalResourceViewResolver();
			resolver.setPrefix(this.mvcProperties.getView().getPrefix());
			resolver.setSuffix(this.mvcProperties.getView().getSuffix());
			return resolver;
		}

		@Bean
		@ConditionalOnBean(View.class)
		@ConditionalOnMissingBean
		public BeanNameViewResolver beanNameViewResolver() {
			BeanNameViewResolver resolver = new BeanNameViewResolver();
			resolver.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
			return resolver;
		}

		@Bean
		@ConditionalOnBean(ViewResolver.class)
		@ConditionalOnMissingBean(name = "viewResolver", value = ContentNegotiatingViewResolver.class)
		public ContentNegotiatingViewResolver viewResolver(BeanFactory beanFactory) {
			ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
			resolver.setContentNegotiationManager(beanFactory.getBean(ContentNegotiationManager.class));
			// ContentNegotiatingViewResolver uses all the other view resolvers to locate
			// a view so it should have a high precedence
			resolver.setOrder(Ordered.HIGHEST_PRECEDENCE);
			return resolver;
		}

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnProperty(prefix = "spring.mvc", name = "locale")
		public LocaleResolver localeResolver() {
			if (this.mvcProperties.getLocaleResolver() == WebMvcProperties.LocaleResolver.FIXED) {
				return new FixedLocaleResolver(this.mvcProperties.getLocale());
			}
			AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
			localeResolver.setDefaultLocale(this.mvcProperties.getLocale());
			return localeResolver;
		}

		@Override
		public MessageCodesResolver getMessageCodesResolver() {
			if (this.mvcProperties.getMessageCodesResolverFormat() != null) {
				DefaultMessageCodesResolver resolver = new DefaultMessageCodesResolver();
				resolver.setMessageCodeFormatter(this.mvcProperties.getMessageCodesResolverFormat());
				return resolver;
			}
			return null;
		}

		@Override
		public void addFormatters(FormatterRegistry registry) {
			ApplicationConversionService.addBeans(registry, this.beanFactory);
		}

		/**
		 * 添加资源处理器，也就是静态资源处理器
		 * @param registry
		 */
		@Override
		public void addResourceHandlers(ResourceHandlerRegistry registry) {
			// resourceProperties是配置文件中的spring.resources的绑定
			// resourceProperties.isAddMappings() 是 spring.resources.addMappings这个配置，默认是true，表示是否可以配置资源处理
			// 默认情况下，静态资源处理是开启的，但是也可以通过配置关闭，配置为false就直接禁用了
			if (!this.resourceProperties.isAddMappings()) {
				// 禁用日志
				logger.debug("Default resource handling disabled");
				// 禁用就直接返回，不添加资源处理器
				return;
			}
			// 静态资源读取还能配置缓存的时间长度，单位是秒，前端会缓存，就不用去再读一次了，可以看到我们可以通过代码得知哪些配置有什么用，这也是看源码的意义
			Duration cachePeriod = this.resourceProperties.getCache().getPeriod();
			// 缓存控制，默认是null，表示不设置缓存
			CacheControl cacheControl = this.resourceProperties.getCache().getCachecontrol().toHttpCacheControl();
			// 如果你的maven导入了webjars，其实就是那些js他封装成了maven 的jar包导入，这里就会注册处理
			if (!registry.hasMappingForPattern("/webjars/**")) {
				customizeResourceHandlerRegistration(registry.addResourceHandler("/webjars/**")
						.addResourceLocations("classpath:/META-INF/resources/webjars/")// 他注册的webjars的路径是类路径下的/META-INF/resources/webjars
						.setCachePeriod(getSeconds(cachePeriod)).setCacheControl(cacheControl));// webjars也会做缓存处理
			}
			// 这里就是静态资源的处理了 staticPathPattern = /**，所以他的处理是对所有请求都会处理，如果能从静态资源目录拿到就去拿到
			// 所以他是对你所有的请求都会处理这里，你访问个啥都会被转到这里，当然了请求高于他，请求要是和资源一样的路径，那先请求拿不到会去拿资源
			String staticPathPattern = this.mvcProperties.getStaticPathPattern();
			if (!registry.hasMappingForPattern(staticPathPattern)) {
				customizeResourceHandlerRegistration(registry.addResourceHandler(staticPathPattern)
						// 他读取的静态资源的目录"classpath:/META-INF/resources/",
						//			"classpath:/resources/", "classpath:/static/", "classpath:/public/"
						.addResourceLocations(getResourceLocations(this.resourceProperties.getStaticLocations()))
						.setCachePeriod(getSeconds(cachePeriod)).setCacheControl(cacheControl));
			}
		}

		private Integer getSeconds(Duration cachePeriod) {
			return (cachePeriod != null) ? (int) cachePeriod.getSeconds() : null;
		}

		private void customizeResourceHandlerRegistration(ResourceHandlerRegistration registration) {
			if (this.resourceHandlerRegistrationCustomizer != null) {
				this.resourceHandlerRegistrationCustomizer.customize(registration);
			}
		}

		@Bean
		@ConditionalOnMissingBean({ RequestContextListener.class, RequestContextFilter.class })
		@ConditionalOnMissingFilterBean(RequestContextFilter.class)
		public static RequestContextFilter requestContextFilter() {
			return new OrderedRequestContextFilter();
		}

	}

	/**
	 * Configuration equivalent to {@code @EnableWebMvc}.
	 */
	@Configuration(proxyBeanMethods = false)
	public static class EnableWebMvcConfiguration extends DelegatingWebMvcConfiguration implements ResourceLoaderAware {

		private final ResourceProperties resourceProperties;

		private final WebMvcProperties mvcProperties;

		private final ListableBeanFactory beanFactory;

		private final WebMvcRegistrations mvcRegistrations;

		private ResourceLoader resourceLoader;

		/**
		 * 这里说一个机制，就是当一个配置类(被@Configuration标记的类)，只有一个全参构造的时候，这个构造器里面所有的参数都会从容器中获取
		 * @param resourceProperties
		 * @param mvcPropertiesProvider
		 * @param mvcRegistrationsProvider
		 * @param beanFactory
		 */
		public EnableWebMvcConfiguration(ResourceProperties resourceProperties,
				ObjectProvider<WebMvcProperties> mvcPropertiesProvider,
				ObjectProvider<WebMvcRegistrations> mvcRegistrationsProvider, ListableBeanFactory beanFactory) {
			this.resourceProperties = resourceProperties;
			this.mvcProperties = mvcPropertiesProvider.getIfAvailable();
			this.mvcRegistrations = mvcRegistrationsProvider.getIfUnique();
			this.beanFactory = beanFactory;
		}

		@Bean
		@Override
		public RequestMappingHandlerAdapter requestMappingHandlerAdapter(
				@Qualifier("mvcContentNegotiationManager") ContentNegotiationManager contentNegotiationManager,
				@Qualifier("mvcConversionService") FormattingConversionService conversionService,
				@Qualifier("mvcValidator") Validator validator) {
			RequestMappingHandlerAdapter adapter = super.requestMappingHandlerAdapter(contentNegotiationManager,
					conversionService, validator);
			adapter.setIgnoreDefaultModelOnRedirect(
					this.mvcProperties == null || this.mvcProperties.isIgnoreDefaultModelOnRedirect());
			return adapter;
		}

		@Override
		protected RequestMappingHandlerAdapter createRequestMappingHandlerAdapter() {
			if (this.mvcRegistrations != null && this.mvcRegistrations.getRequestMappingHandlerAdapter() != null) {
				return this.mvcRegistrations.getRequestMappingHandlerAdapter();
			}
			return super.createRequestMappingHandlerAdapter();
		}

		@Bean
		@Primary
		@Override
		public RequestMappingHandlerMapping requestMappingHandlerMapping(
				@Qualifier("mvcContentNegotiationManager") ContentNegotiationManager contentNegotiationManager,
				@Qualifier("mvcConversionService") FormattingConversionService conversionService,
				@Qualifier("mvcResourceUrlProvider") ResourceUrlProvider resourceUrlProvider) {
			// Must be @Primary for MvcUriComponentsBuilder to work
			return super.requestMappingHandlerMapping(contentNegotiationManager, conversionService,
					resourceUrlProvider);
		}

		@Bean
		public WelcomePageHandlerMapping welcomePageHandlerMapping(ApplicationContext applicationContext,
				FormattingConversionService mvcConversionService, ResourceUrlProvider mvcResourceUrlProvider) {
			WelcomePageHandlerMapping welcomePageHandlerMapping = new WelcomePageHandlerMapping(
					new TemplateAvailabilityProviders(applicationContext), applicationContext, getWelcomePage(),
					this.mvcProperties.getStaticPathPattern());
			welcomePageHandlerMapping.setInterceptors(getInterceptors(mvcConversionService, mvcResourceUrlProvider));
			welcomePageHandlerMapping.setCorsConfigurations(getCorsConfigurations());
			return welcomePageHandlerMapping;
		}

		private Optional<Resource> getWelcomePage() {
			String[] locations = getResourceLocations(this.resourceProperties.getStaticLocations());
			return Arrays.stream(locations).map(this::getIndexHtml).filter(this::isReadable).findFirst();
		}

		private Resource getIndexHtml(String location) {
			return this.resourceLoader.getResource(location + "index.html");
		}

		private boolean isReadable(Resource resource) {
			try {
				return resource.exists() && (resource.getURL() != null);
			}
			catch (Exception ex) {
				return false;
			}
		}

		@Bean
		@Override
		public FormattingConversionService mvcConversionService() {
			Format format = this.mvcProperties.getFormat();
			WebConversionService conversionService = new WebConversionService(new DateTimeFormatters()
					.dateFormat(format.getDate()).timeFormat(format.getTime()).dateTimeFormat(format.getDateTime()));
			addFormatters(conversionService);
			return conversionService;
		}

		@Bean
		@Override
		public Validator mvcValidator() {
			if (!ClassUtils.isPresent("javax.validation.Validator", getClass().getClassLoader())) {
				return super.mvcValidator();
			}
			return ValidatorAdapter.get(getApplicationContext(), getValidator());
		}

		@Override
		protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
			if (this.mvcRegistrations != null && this.mvcRegistrations.getRequestMappingHandlerMapping() != null) {
				return this.mvcRegistrations.getRequestMappingHandlerMapping();
			}
			return super.createRequestMappingHandlerMapping();
		}

		@Override
		protected ConfigurableWebBindingInitializer getConfigurableWebBindingInitializer(
				FormattingConversionService mvcConversionService, Validator mvcValidator) {
			try {
				return this.beanFactory.getBean(ConfigurableWebBindingInitializer.class);
			}
			catch (NoSuchBeanDefinitionException ex) {
				return super.getConfigurableWebBindingInitializer(mvcConversionService, mvcValidator);
			}
		}

		@Override
		protected ExceptionHandlerExceptionResolver createExceptionHandlerExceptionResolver() {
			if (this.mvcRegistrations != null && this.mvcRegistrations.getExceptionHandlerExceptionResolver() != null) {
				return this.mvcRegistrations.getExceptionHandlerExceptionResolver();
			}
			return super.createExceptionHandlerExceptionResolver();
		}

		@Override
		protected void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
			super.extendHandlerExceptionResolvers(exceptionResolvers);
			if (this.mvcProperties.isLogResolvedException()) {
				for (HandlerExceptionResolver resolver : exceptionResolvers) {
					if (resolver instanceof AbstractHandlerExceptionResolver) {
						((AbstractHandlerExceptionResolver) resolver).setWarnLogCategory(resolver.getClass().getName());
					}
				}
			}
		}

		@Bean
		@Override
		public ContentNegotiationManager mvcContentNegotiationManager() {
			ContentNegotiationManager manager = super.mvcContentNegotiationManager();
			List<ContentNegotiationStrategy> strategies = manager.getStrategies();
			ListIterator<ContentNegotiationStrategy> iterator = strategies.listIterator();
			while (iterator.hasNext()) {
				ContentNegotiationStrategy strategy = iterator.next();
				if (strategy instanceof PathExtensionContentNegotiationStrategy) {
					iterator.set(new OptionalPathExtensionContentNegotiationStrategy(strategy));
				}
			}
			return manager;
		}

		@Override
		public void setResourceLoader(ResourceLoader resourceLoader) {
			this.resourceLoader = resourceLoader;
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnEnabledResourceChain
	static class ResourceChainCustomizerConfiguration {

		@Bean
		ResourceChainResourceHandlerRegistrationCustomizer resourceHandlerRegistrationCustomizer() {
			return new ResourceChainResourceHandlerRegistrationCustomizer();
		}

	}

	interface ResourceHandlerRegistrationCustomizer {

		void customize(ResourceHandlerRegistration registration);

	}

	static class ResourceChainResourceHandlerRegistrationCustomizer implements ResourceHandlerRegistrationCustomizer {

		@Autowired
		private ResourceProperties resourceProperties = new ResourceProperties();

		@Override
		public void customize(ResourceHandlerRegistration registration) {
			ResourceProperties.Chain properties = this.resourceProperties.getChain();
			configureResourceChain(properties, registration.resourceChain(properties.isCache()));
		}

		private void configureResourceChain(ResourceProperties.Chain properties, ResourceChainRegistration chain) {
			Strategy strategy = properties.getStrategy();
			if (properties.isCompressed()) {
				chain.addResolver(new EncodedResourceResolver());
			}
			if (strategy.getFixed().isEnabled() || strategy.getContent().isEnabled()) {
				chain.addResolver(getVersionResourceResolver(strategy));
			}
			if (properties.isHtmlApplicationCache()) {
				chain.addTransformer(new AppCacheManifestTransformer());
			}
		}

		private ResourceResolver getVersionResourceResolver(ResourceProperties.Strategy properties) {
			VersionResourceResolver resolver = new VersionResourceResolver();
			if (properties.getFixed().isEnabled()) {
				String version = properties.getFixed().getVersion();
				String[] paths = properties.getFixed().getPaths();
				resolver.addFixedVersionStrategy(version, paths);
			}
			if (properties.getContent().isEnabled()) {
				String[] paths = properties.getContent().getPaths();
				resolver.addContentVersionStrategy(paths);
			}
			return resolver;
		}

	}

	/**
	 * Decorator to make {@link PathExtensionContentNegotiationStrategy} optional
	 * depending on a request attribute.
	 */
	static class OptionalPathExtensionContentNegotiationStrategy implements ContentNegotiationStrategy {

		private static final String SKIP_ATTRIBUTE = PathExtensionContentNegotiationStrategy.class.getName() + ".SKIP";

		private final ContentNegotiationStrategy delegate;

		OptionalPathExtensionContentNegotiationStrategy(ContentNegotiationStrategy delegate) {
			this.delegate = delegate;
		}

		@Override
		public List<MediaType> resolveMediaTypes(NativeWebRequest webRequest)
				throws HttpMediaTypeNotAcceptableException {
			Object skip = webRequest.getAttribute(SKIP_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
			if (skip != null && Boolean.parseBoolean(skip.toString())) {
				return MEDIA_TYPE_ALL_LIST;
			}
			return this.delegate.resolveMediaTypes(webRequest);
		}

	}

}
