package org.elevans.migration.resource

import java.io.{InputStream, FileNotFoundException}
import org.apache.commons.io.{FilenameUtils, Charsets, IOUtils}
import org.elevans.migration.Transition
import org.elevans.migration.parse.TransitionMetadataParser
import com.typesafe.scalalogging.{LazyLogging => Logging}

/**
 * A `Transition` that is embodied in the specified resource.
 *
 * @author Eric Evans
 */
case class ResourceTransition(resourcePath: String,
                              beforeState: String,
                              afterState: String,
                              isDestructive: Boolean) extends Transition

/**
 * Utility for finding and parsing Transition metadata from resource files on the classpath.
 *
 * @param classLoader           ClassLoader whose classpath will be used to find Transition resources.
 * @param resourceDirectoryPath path to resource "directory" in the classpath that might contain Transition resources.
 * @param transitionParser      TransitionMetadataParser to use for extracting TransitionMetadata from resource files.
 * @param resourceResolvers     Seq of ResourceResolvers to use, in order, to look for resources on the classpath;
 *                              the first one which returns
 *
 * @throws FileNotFoundException if resourceDirectory does not exist or is ""
 *
 * @author Eric Evans
 */
class ResourceTransitionFinder(val classLoader: ClassLoader,
                               val resourceDirectoryPath: String,
                               val transitionParser: TransitionMetadataParser,
                               val resourceResolvers: Seq[ResourceResolver]) extends Logging {

  lazy val normalizedResourceDirectory = FilenameUtils.normalizeNoEndSeparator(resourceDirectoryPath.dropWhile(_ == '/'), true)

  /** Return the path of every resource found within the resourceDirectoryPath. */
  def getResourcePaths: Set[String] = {
    val url = Option(classLoader.getResource(normalizedResourceDirectory))
      .getOrElse( throw new FileNotFoundException(s"Resource directory '$normalizedResourceDirectory'") )

    logger.debug(s"url = '$url'")

    // Use a lazy view of "resourceResolvers" so that we return after the first call to getResourcePaths() that returns results.
    resourceResolvers.view.flatMap(_.getResourceNames(url)).headOption.getOrElse(Set.empty).map{name => s"$normalizedResourceDirectory/$name".stripPrefix("/")}
//    resourceResolvers.view.flatMap(_.getResourcePaths(dirUrl, normalizedResourceDirectory)).headOption.getOrElse(Set.empty)
  }

  /**
   * Apply the transitionParser to every resource found within the resourceDirectoryPath, and return
   * a Set of ResourceFileTransitions for those that contain the required Transition metadata.
   */
  def getTransitions: Set[ResourceTransition] = getResourcePaths.flatMap { resourcePath =>
    Option(classLoader.getResourceAsStream(resourcePath)) map { in =>
      try {
        val content = IOUtils.toString(in, Charsets.UTF_8)
        transitionParser.parse(content) map {m => ResourceTransition(resourcePath, m.beforeState, m.afterState, m.isDestructive)}
      } finally {
        in.close()
      }
    } getOrElse( throw new FileNotFoundException(s"Resource '$resourcePath' in class loader '$classLoader'") )
  }.toSet

  /**    */
  def getInputStream(transition: ResourceTransition): Option[InputStream] = getInputStream(transition.resourcePath)

  /**    */
  def getInputStream(resourcePath: String): Option[InputStream] = Option(classLoader.getResourceAsStream(resourcePath))
}
