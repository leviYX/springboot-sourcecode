package smoketest.simple.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import smoketest.simple.controller.MyController;
import smoketest.simple.exception.MyException;

import javax.servlet.http.HttpServletRequest;

// 异常处理我们自己的controller，实际你不配就是整个包
@ControllerAdvice(basePackageClasses = MyController.class)
public class AcmeControllerAdvice extends ResponseEntityExceptionHandler {

	// 我们这里处理的异常就是我们自己定义的异常MyException，实际开发，这里可以配置多个，然后根据异常类型来处理，你自己灵活处理就行
	@ExceptionHandler(MyException.class)
	public String handleControllerException(HttpServletRequest request, Throwable ex) {
		// 返回我们的视图名称，如果正常，他就会跳转去这个页面
		return "controllerAdviceHtml";
	}
}