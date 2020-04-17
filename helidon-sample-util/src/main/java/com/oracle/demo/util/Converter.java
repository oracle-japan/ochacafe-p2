package com.oracle.demo.util;

import java.util.Optional;

public interface Converter {
	public static Optional<Integer> toInteger(String str) {
		try {
			return Optional.of(Integer.valueOf(Optional.of(str).orElse("0")));
		} catch (NumberFormatException ex) {
			return Optional.of(Integer.valueOf(0));
		}
	}

}
