package gov.hawaii.digitalarchives.hida.core.model;

import java.util.Set;


/**
 * This is an interface for handling {@link Event}s.
 * 
 * @author Calvin Wong
 * 
 */
public interface Eventful
{
    /**
     * Add a new {@link Event} for this object. Duplicate {@link Event}s (e.g.
     * {@link Event}s that have already been previously added) are ignored.
     * 
     * @param event The {@link Event} to add for this Object.
     */
    void addEvent (Event event);

    /**
     * @return The list of {@link Event}s that have taken place for this object.
     */
    Set<Event> getEvents ();

}
