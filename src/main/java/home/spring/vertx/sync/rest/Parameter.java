package home.spring.vertx.sync.rest;

/**
 * Class for support parameters submitted, with key value form.
 * @author aardelean
 *
 */
public class Parameter {
	private final String key;

	private final String value;

	public Parameter(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

}