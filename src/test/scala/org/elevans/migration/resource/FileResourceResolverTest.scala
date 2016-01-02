package org.elevans.migration.resource

import java.net.URL
import java.io.{FileNotFoundException, File}
import org.scalatest.{Matchers, FunSpec}

class FileResourceResolverTest extends FunSpec with Matchers {

  describe("FileResourceResolver") {

    val testResourcesDir = new File(".").getCanonicalPath + "/target/test-classes"

    it("should find resources in the directory specified by the URL") {
      FileResourceResolver.getResourceNames(new URL(s"file:$testResourcesDir/")).map(_.filterNot(_.contains("Timber"))) shouldBe
        Some(Set("test-transitions.jar", "A_toB_destructiveFalse.txt", "B_toA_destructiveTrue.txt", "B_toC_destructiveFalse.txt", "C_toB_destructiveTrue.txt"))
    }
    it("should return empty Set of resources if there are none in the directory specified by the URL") {
      FileResourceResolver.getResourceNames(new URL(s"file:$testResourcesDir/com/foo")) shouldBe Some(Set.empty)
    }
    it("should return None if the URL does not have the 'file' protocol") {
      FileResourceResolver.getResourceNames(new URL(s"http:$testResourcesDir")) shouldBe None
    }
    it("should throw FileNotFoundException if the URL does not specify a directory") {
      an [FileNotFoundException] should be thrownBy
        FileResourceResolver.getResourceNames(new URL(s"file:$testResourcesDir/com/foo/BOGUS"))
    }
  }
}
