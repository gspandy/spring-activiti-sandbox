package org.activiti.spring.test.components.scope;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * dumb object to demonstrate holding scoped state for the duration of a business process
 *
 * @author Josh Long
 */
public class StatefulObject implements Serializable {

	private transient Logger logger = Logger.getLogger(getClass().getName());

	public static final long serialVersionUID = 1L;

	private String name;
	private int visitedCount = 0;

	public StatefulObject() {
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StatefulObject that = (StatefulObject) o;

		if (visitedCount != that.visitedCount) return false;
		if (name != null ? !name.equals(that.name) : that.name != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + visitedCount;
		return result;
	}

	@Override
	public String toString() {
		return "StatefulObject{" +
				"name='" + name + '\'' +
				", visitedCount=" + visitedCount +
				'}';
	}

	public void increment() {
		this.visitedCount += 1;
	}

	public int getVisitedCount() {
		return this.visitedCount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
