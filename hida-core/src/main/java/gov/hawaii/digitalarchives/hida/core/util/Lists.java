
package gov.hawaii.digitalarchives.hida.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Convenience helper class for instantiating arrays.
 *
 * @author Dongie Agnir
 */
public class Lists {
	
	/**
	 * @return A new empty list for containing instances of type {@code T}.
	 */
	public static <T> List<T> newArrayList() {
		return new ArrayList<T>();
	}

    /**
     * Creates a new {@code ArrayList} copying the contents of {@code list}.
     *
     * @param list The collection to make a copy of.
     * @param <T> The type contained in the {@code Collection}, and to be
     *           contained in the {@code ArrayList}.
     * @return The newly created {@code ArrayList}.
     */
    public static <T> ArrayList<T> newArrayList(Collection<T> list) {
        return new ArrayList<T>(list);
    }
}
