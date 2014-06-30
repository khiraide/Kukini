package gov.hawaii.digitalarchives.hida.space;

/**
 * Factory class used instantiate a {@link Space}.
 */
public interface SpaceCreator {
    /**
     * Create a new instance of a {@link Space} with the given name.
     *
     * @param spaceName The name of the space.
     *
     * @return The newly created {@code Space}.
     */
    public Space createSpace(String spaceName);
}
