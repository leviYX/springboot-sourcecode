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

package org.springframework.boot.autoconfigure.web.servlet.error;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Basic global error {@link Controller @Controller}, rendering {@link ErrorAttributes}.
 * More specific errors can be handled either using Spring MVC abstractions (e.g.
 * {@code @ExceptionHandler}) or by adding servlet
 * {@link AbstractServletWebServerFactory#setErrorPages server error pages}.
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author Michael Stummvoll
 * @author Stephane Nicoll
 * @author Scott Frederick
 * @since 1.0.0
 * @see ErrorAttributes
 * @see ErrorProperties
 */

/**
 * 这是一个controller，所以这个controller和我们写的没啥区别，我们看到他的请求路径是这样的
 *${server.error.path:${error.path:/error}} 表达式的意思是：首先尝试解析 server.error.path 属性。如果该属性未定义，
 * 则使用 error.path 属性。如果 error.path 也未定义，则使用默认路径 /error。所以我们这里就可以知道，
 * 他的异常处理默认请求的controller大路径是/error，当然我们也可以通过配置文件来修改这个默认的请求路径。你改了就用你的了
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class BasicErrorController extends AbstractErrorController {

	private final ErrorProperties errorProperties;

	/**
	 * Create a new {@link BasicErrorController} instance.
	 * @param errorAttributes the error attributes
	 * @param errorProperties configuration properties
	 */
	public BasicErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties) {
		this(errorAttributes, errorProperties, Collections.emptyList());
	}

	/**
	 * Create a new {@link BasicErrorController} instance.
	 * @param errorAttributes the error attributes
	 * @param errorProperties configuration properties
	 * @param errorViewResolvers error view resolvers
	 */
	public BasicErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties,
			List<ErrorViewResolver> errorViewResolvers) {
		super(errorAttributes, errorViewResolvers);
		Assert.notNull(errorProperties, "ErrorProperties must not be null");
		this.errorProperties = errorProperties;
	}

	@Override
	public String getErrorPath() {
		return null;
	}

	/**
	 * 我们看到这里是异常html的处理，所以当你请求的异常是在页面的时候produces = MediaType.TEXT_HTML_VALUE
	 * 此时就会进入这个方法，然后返回一个ModelAndView对象，这个对象里面包含了错误信息，并且给你跳转去
	 * 错误页面，所以这个方法就是处理异常的。浏览器请求接口的异常来这里，然后通过ModelAndView跳去异常视图
	 */
	@RequestMapping(produces = MediaType.TEXT_HTML_VALUE)// "text/html"
	public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
		HttpStatus status = getStatus(request);
		Map<String, Object> model = Collections
				.unmodifiableMap(getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.TEXT_HTML)));
		response.setStatus(status.value());
		// 构建异常视图，给前端返回
		ModelAndView modelAndView = resolveErrorView(request, response, status, model);
		// 页面响应响应error这个页面
		return (modelAndView != null) ? modelAndView : new ModelAndView("error", model);
	}

	/**
	 * 非页面的响应，在这里处理，直接返回json数据，比如我们用postman测试的时候，就会进入这个方法，不是给html页面响应
	 * ResponseEntity返回类型就是字符串类型，其实就是个json
	 */
	@RequestMapping
	public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
		HttpStatus status = getStatus(request);
		if (status == HttpStatus.NO_CONTENT) {
			return new ResponseEntity<>(status);
		}
		Map<String, Object> body = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
		return new ResponseEntity<>(body, status);
	}

	@ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
	public ResponseEntity<String> mediaTypeNotAcceptable(HttpServletRequest request) {
		HttpStatus status = getStatus(request);
		return ResponseEntity.status(status).build();
	}

	protected ErrorAttributeOptions getErrorAttributeOptions(HttpServletRequest request, MediaType mediaType) {
		ErrorAttributeOptions options = ErrorAttributeOptions.defaults();
		if (this.errorProperties.isIncludeException()) {
			options = options.including(Include.EXCEPTION);
		}
		if (isIncludeStackTrace(request, mediaType)) {
			options = options.including(Include.STACK_TRACE);
		}
		if (isIncludeMessage(request, mediaType)) {
			options = options.including(Include.MESSAGE);
		}
		if (isIncludeBindingErrors(request, mediaType)) {
			options = options.including(Include.BINDING_ERRORS);
		}
		return options;
	}

	/**
	 * Determine if the stacktrace attribute should be included.
	 * @param request the source request
	 * @param produces the media type produced (or {@code MediaType.ALL})
	 * @return if the stacktrace attribute should be included
	 */
	@SuppressWarnings("deprecation")
	protected boolean isIncludeStackTrace(HttpServletRequest request, MediaType produces) {
		switch (getErrorProperties().getIncludeStacktrace()) {
		case ALWAYS:
			return true;
		case ON_PARAM:
		case ON_TRACE_PARAM:
			return getTraceParameter(request);
		default:
			return false;
		}
	}

	/**
	 * Determine if the message attribute should be included.
	 * @param request the source request
	 * @param produces the media type produced (or {@code MediaType.ALL})
	 * @return if the message attribute should be included
	 */
	protected boolean isIncludeMessage(HttpServletRequest request, MediaType produces) {
		switch (getErrorProperties().getIncludeMessage()) {
		case ALWAYS:
			return true;
		case ON_PARAM:
			return getMessageParameter(request);
		default:
			return false;
		}
	}

	/**
	 * Determine if the errors attribute should be included.
	 * @param request the source request
	 * @param produces the media type produced (or {@code MediaType.ALL})
	 * @return if the errors attribute should be included
	 */
	protected boolean isIncludeBindingErrors(HttpServletRequest request, MediaType produces) {
		switch (getErrorProperties().getIncludeBindingErrors()) {
		case ALWAYS:
			return true;
		case ON_PARAM:
			return getErrorsParameter(request);
		default:
			return false;
		}
	}

	/**
	 * Provide access to the error properties.
	 * @return the error properties
	 */
	protected ErrorProperties getErrorProperties() {
		return this.errorProperties;
	}

}
