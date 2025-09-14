package no.uib.inf102.util;

import java.util.Map;

public class MapUtil {

	public static <T> void increase(T elem, Map<T, Integer> map) {
		map.put(elem, map.getOrDefault(elem, 0)+1);
	}

	public static int sum(Map<?, Integer> map) {
		int sum=0;
		for(Integer num : map.values()) {
			sum+=num;
		}
		return sum;
	}
}
