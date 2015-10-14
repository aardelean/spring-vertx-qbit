package home.spring.vertx.sync.rest.transport;

import java.io.Serializable;

/**
 * Basic result for an operation invoked by the rest api.
 * @author aardelean
 *
 */
public class DefaultResult implements Serializable {

	private boolean success = true;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

}
