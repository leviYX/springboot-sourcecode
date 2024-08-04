package smoketest.simple.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 通过在异常上标注注解，来定义异常的状态码和返回信息
@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Status Exception")
public class StatusException extends RuntimeException{

	public StatusException() {
	}

	public StatusException(String message) {
		super(message);
	}

	public StatusException(String message, Throwable cause) {
		super(message, cause);
	}

	public StatusException(Throwable cause) {
		super(cause);
	}

	public StatusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
