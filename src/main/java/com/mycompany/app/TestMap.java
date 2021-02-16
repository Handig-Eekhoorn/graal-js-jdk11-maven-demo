package com.mycompany.app;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

public class TestMap implements Map<String,Object>, ProxyObject {

	public String key2="is a field";

	private final Map<String,Object> realMap = new TreeMap<String, Object>();
	
	public TestMap() {
		realMap.put("key1", "Key1 from map");
		realMap.put("key2", "Key2 from map");
	}
	
	@Override
	public void clear() {
		this.realMap.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return this.realMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return this.realMap.containsValue(value);
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return this.realMap.entrySet();
	}

	@Override
	public Object get(Object key) {
		System.out.println("map.get["+key+"]");
		return this.realMap.get(key);
	}

	@Override
	public boolean isEmpty() {
		return this.realMap.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return this.realMap.keySet();
	}

	@Override
	public Object put(String key, Object value) {
		return this.realMap.put(key, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		this.realMap.putAll(m);
	}

	@Override
	public Object remove(Object key) {
		return this.realMap.remove(key);
	}

	@Override
	public int size() {
		return this.realMap.size();
	}

	@Override
	public Collection<Object> values() {
		return this.realMap.values();
	}

	/*
	 * ProxyObject methods
	 */
	@Override
	public Object getMember(String key) {
		try {
			return TestMap.class.getDeclaredMethod(key);
		} catch (Exception e) {
			System.err.println("getMember["+key+"]: "+e);
		}
		return this.get(key);
	}

	@Override
	public Object getMemberKeys() {
		return this.keySet();
	}

	@Override
	public boolean hasMember(String key) {
		return this.containsKey(key);
	}

	@Override
	public void putMember(String key, Value value) {
		this.put(key, value);
	}

}
