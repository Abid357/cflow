package helpers;

import globals.LOGGER;

public class MyException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final ErrorType type;

	public MyException(ErrorType type) {
		super();
		this.type = type;
	}

	public MyException(String message, Throwable cause, ErrorType type) {
		super(message, cause);
		this.type = type;
		LOGGER.Error.log(cause.getStackTrace().toString());
	}

	public MyException(String message, ErrorType type) {
		super(message);
		this.type = type;
	}

	public MyException(Throwable cause, ErrorType type) {
		super(cause);
		this.type = type;
	}

	public ErrorType getType() {
		return this.type;
	}
}
