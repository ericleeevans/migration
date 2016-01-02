// Copyright 2015-2016 Eric Evans : Scala migration
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.elevans.migration.resource

import java.io.{File, FileNotFoundException}
import java.net.{URLDecoder, JarURLConnection, URL}
import org.osgi.framework.BundleContext
import org.osgi.framework.wiring.BundleWiring
import com.typesafe.scalalogging.{LazyLogging => Logging}

/**
 * Trait for resolving transition resources under a given `URL`.
 *
 * @author Eric Evans
 */
trait ResourceResolver {
  /**
   * If the protocol of the given `URL` is supported, return a `Set` containing the name of every resource found directly
   * within the URL's path (non-recursive); if the URL protocol is not supported, then return None.
   *
   * @param url URL that might contain resources
   *
   * @return Some Set of resource names in the URL's path, or None if the URL's protocol is not supported
   */
  def getResourceNames(url: URL): Option[Set[String]]
}

/**
 * `ResourceResolver` for resources within a JAR file (URL protocol "`jar:`").
 *
 * @author Eric Evans
 */
object JarFileResourceResolver extends ResourceResolver with Logging {

  def getResourceNames(url: URL) = {
    import scala.collection.JavaConversions._

    if (url.getProtocol == "jar") {
      val urlPath = URLDecoder.decode(url.getPath, "UTF-8")
      val resourceDirectory = urlPath.substring(urlPath.lastIndexOf('!') + 2)
      val jarFile = url.openConnection.asInstanceOf[JarURLConnection].getJarFile
      try {
        val resourceEntries = jarFile.entries.map(_.getName).filter{name => name.startsWith(resourceDirectory) && !name.endsWith("/")}.toSet
        val resources = resourceEntries.map(_.stripPrefix(resourceDirectory).stripPrefix("/")).filter(_.split("/").length < 2)
        Some( resources )
      }
      finally {
        jarFile.close()
      }
    }
    else {
      logger.debug(s"'url' is not a JAR file URL")
      None
    }
  }
}

/**
 * `ResourceResolver` for resources in the filesystem (URL protocol "`file:`").
 *
 * @author Eric Evans
 */
object FileResourceResolver extends ResourceResolver with Logging {

  def getResourceNames(url: URL) = {
    if (url.getProtocol == "file") {
      val resourceDirectory = new File(URLDecoder.decode(url.getPath, "UTF-8"))
      if (resourceDirectory.isDirectory) {
        val resources = resourceDirectory.listFiles().filter(_.isFile).map(f => s"${f.getName}".stripPrefix("/")).toSet
        Some( resources )
      }
      else throw new FileNotFoundException(s"URL '$url' does not specify a directory.")
    }
    else {
      logger.debug(s"'url' is not a file URL")
      None
    }
  }
}

/**
 * `ResourceResolver` for resources within an OSGi bundle (URL protocol "`bundle:`").
 *
 * @param bundleContext BundleContext within which to resolve resources.
 *
 * @author Eric Evans
 */
class BundleResourceResolver(bundleContext: BundleContext) extends ResourceResolver with Logging {
  import scala.collection.JavaConversions._

  def getResourceNames(url: URL) = {
    if (url.getProtocol == "bundle") {
      val resourceDirectory = URLDecoder.decode(url.getPath, "UTF-8").stripPrefix("/")
      val bundleWiring = bundleContext.getBundle.adapt(classOf[BundleWiring])
      val resources = bundleWiring.listResources(resourceDirectory, "*", 0).toSet[String].map(_.stripPrefix(resourceDirectory).stripPrefix("/"))
      Some( resources )
    }
    else {
      logger.debug(s"'url' is not an OSGi bundle URL")
      None
    }
  }
}
