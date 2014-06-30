/** <p>
 * The Bag module encapsulates the knowledge of Bag structure, naming
 * conventions, tag files including manifest, and fixity. It can also zip and
 * unzip Bags. The Bag movement operations also encapsulate retry behavior. In
 * the case of a copy or move failure this module will retry as configured. This
 * module also provides actions for copying and moving Bags with fixity checking
 * to guarantee the integrity of the Bags. The scope of the Bag module is
 * limited to BagIt Bags, and does not include any knowledge of application
 * specific semantics such as SIPs or RTPs or application specific metadata.
 * (See SIP Bag Checker Module for application specific business logic).
 * </p>
 * <p>
 * Bag operations can act on bags as directories, or as zip archive files.
 * </p>
 * 
 * @author Calvin Wong */
package gov.hawaii.digitalarchives.hida.bag;