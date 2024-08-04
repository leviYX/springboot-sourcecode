package smoketest.simple.handler;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// 优先级最高，数字越小，优先级越高
//@Order(Integer.MIN_VALUE)
// 放入容器
//@Component
public class MyHandlerExceptionResolver implements HandlerExceptionResolver {
	@Override
	public ModelAndView resolveException(HttpServletRequest request,
										 HttpServletResponse response,
										 Object handler,
										 Exception ex) {
        try {
			/**
			 * 这里直接发tomcat，和前端交互，其实后面的就都没用了
			 */
            response.sendError(540,"levi define error message");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
		// 返回个空，这里也没啥用，后面都是没业务能力的了
        return new ModelAndView();
	}
}
