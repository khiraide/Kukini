/** The {@link Space} implementation is designed to sit on top of a Hazelcast
 * Instance.  Within a single process (JVM), there is usually a single
 * {@code HazelcastInstance}, that all instances of a {@code Space} share.
 * Furthermore, Spaces should not be created directly, but rather by a {@link
 * SpaceCreator}.  {@code SpaceCreator}s have a single {@code
 * HazelcastInstance}  that is given to it via injection, which it then gives
 * to each {@code Space} it creates.
 * <p>
 * Also, it is important to note that {@link SpaceImpl} has not been designed
 * to be thread-safe.  Each thread that wishes to use as {@code Space} should
 * create its own instance of a {@code Space} using the {@code SpaceCreator},
 * instead of sharing them across threads.  All {@code Space} operations are
 * ultimately delegated to distributed objects (e.g. {@code
 * com.hazelcast.IMap}) provided by the singular {@code HazelcastInstance};
 * these objects ARE designed to be concurrent and be thread-safe.
 * <p>
 * See <a href=https://hera.digitalarchives.hawaii.gov/confluence/display/DEV/Space+Module#sectionId=0">The Space Module Documentation</a> for more information oon the architecture of the Space Module, and how to
 * use it.
 */
package gov.hawaii.digitalarchives.hida.space;
