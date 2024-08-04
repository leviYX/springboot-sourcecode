package smoketest.simple.controller;


import org.springframework.web.bind.annotation.*;
import smoketest.simple.exception.MyException;
import smoketest.simple.exception.StatusException;

@RestController
@RequestMapping("/test")
public class MyController {


	@GetMapping("testError")
	public String test() {
		int i = 1 / 0;
		return "error";
	}

	@GetMapping("testControllerAdvice")
	public String testControllerAdvice() {
		// 测试 ControllerAdvice，抛出我们自己的异常
		throw new MyException("testControllerAdvice error");
	}

	@GetMapping("testResponseStatus")
	public String testResponseStatus() {
		// 测试 StatusException，抛出我们自己的异常
		throw new StatusException();
	}

	@GetMapping("testInnerException")
	public String testInnerException(@RequestParam(name = "a") String a) {
		// 测试 内部异常
		return a;
	}
}
