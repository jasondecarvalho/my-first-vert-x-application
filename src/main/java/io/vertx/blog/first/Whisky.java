package io.vertx.blog.first;

import io.vertx.core.json.JsonObject;

public class Whisky {

	private String id;
	private String name;
	private String origin;

	public Whisky(String name, String origin) {
		this.name = name;
		this.origin = origin;
	}

	public Whisky() {
		//
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public JsonObject asJsonObject() {
		return new JsonObject().put("name", name).put("origin", origin);
	}
}