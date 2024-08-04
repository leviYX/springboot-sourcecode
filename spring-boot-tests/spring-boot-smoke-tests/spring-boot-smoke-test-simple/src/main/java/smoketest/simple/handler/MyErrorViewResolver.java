package smoketest.simple.handler;

import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.util.Map;

// 优先级最高，数字越小，优先级越高
@Order(Integer.MIN_VALUE)
// 放入容器
@Component
public class MyErrorViewResolver implements ErrorViewResolver {
	@Override
	public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
		return null;
	}
}
