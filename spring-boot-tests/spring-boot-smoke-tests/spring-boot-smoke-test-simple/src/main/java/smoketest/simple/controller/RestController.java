package smoketest.simple.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@org.springframework.web.bind.annotation.RestController
public class RestController {


	@RequestMapping(value = "/user" ,method = RequestMethod.GET)
	public String getUser() {
		return "getUser";
	}

	@RequestMapping(value = "/user" ,method = RequestMethod.POST)
	public String postUser() {
		return "postUser";
	}

	@RequestMapping(value = "/user" ,method = RequestMethod.PUT)
	public String putUser() {
		return "putUser";
	}

	@RequestMapping(value = "/user" ,method = RequestMethod.DELETE)
	public String delUser() {
		return "delUser";
	}
}
