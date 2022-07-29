package hr.cache.object;

import java.util.Date;

public class ExpireObject {
	private long createdDateTime;
	private Object valueObject;
	
	public static ExpireObject getNewInstance() {
		return new ExpireObject();
	}
	
	private ExpireObject() {
		createdDateTime = new Date().getTime();
	}
	
	public Object getValueObject() {
		return valueObject;
	}
	public void setValueObject(Object valueObject) {
		this.valueObject = valueObject;
	}
	public long getCreatedDateTime() {
		return createdDateTime;
	}
}
