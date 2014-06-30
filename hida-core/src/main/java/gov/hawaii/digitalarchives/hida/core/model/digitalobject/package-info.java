/**
 * This package contains object models directly related to the Digital Object
 * Model.  The most important object in this package is {@link FileObject},
 * which is meant to be a Digital Object that represents a file on the local
 * filesystem, and all of its associated metadata.
 * <p>
 * Note that within the Hawaii Digital Archives Project, a "digital object" may
 * refer to two different but related things depending on the context: in one
 * sense, a digital object in a strictly archival sense, is an "object composed
 * of a set of bit sequences"--essentially a file, or bitstream.  However, a
 * "digital object" is also on a higher level than just a file, which also
 * contains the metadata associated with the actual file.  {@link FileObject}
 * encapsulates the file's bits, as well as its metatada.
 * <p>
 * @author Dongie Agnir
 */
package gov.hawaii.digitalarchives.hida.core.model.digitalobject;
