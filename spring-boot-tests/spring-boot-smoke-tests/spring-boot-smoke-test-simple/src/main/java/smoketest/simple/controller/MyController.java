package smoketest.simple.controller;


import org.springframework.web.bind.annotation.*;
import smoketest.simple.MyNo;

@RestController
@RequestMapping("/test")
public class MyController {

	/*@RequestMapping("test")
	public MypO test(@MyNo(no = "sb")  MypO mypO) {
		return mypO;
	}*/

	@GetMapping("testError")
	public String test() {
		int i = 1 / 0;
		return "error";
	}
}
