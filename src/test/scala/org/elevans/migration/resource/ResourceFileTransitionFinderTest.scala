package org.elevans.migration.resource

import org.scalatest.{Matchers, FunSpec}
import org.elevans.migration.parse.KeyValueTransitionMetadataParser

class ResourceFileTransitionFinderTest extends FunSpec with Matchers {

  lazy val transitionParser = new KeyValueTransitionMetadataParser("transition-before", "transition-after", "transition-destructive", "=")
  val resourceResolvers = Seq( FileResourceResolver, JarFileResourceResolver )

  trait TextFixture {
    val testTransitionsResourceDirectory: String

    lazy val resourceTransitionFinder = new ResourceTransitionFinder(this.getClass.getClassLoader, testTransitionsResourceDirectory, transitionParser, resourceResolvers)

    def transition(filename: String, before: String, after: String, isDestructive: Boolean) = {
      ResourceTransition(s"${resourceTransitionFinder.normalizedResourceDirectory}/$filename".stripPrefix("/"), before, after, isDestructive)
    }

    lazy val rootLevelTestTransitions = Set(transition("A_toB_destructiveFalse.txt", "A", "B", false),
                                            transition("B_toA_destructiveTrue.txt", "B", "A", true),
                                            transition("B_toC_destructiveFalse.txt", "B", "C", false),
                                            transition("C_toB_destructiveTrue.txt", "C", "B", true))

    lazy val deepLevelTestTransitions = Set(transition("fromA_toB_destructiveFalse.txt", "A", "B", false),
                                            transition("fromB_toA_destructiveTrue.txt", "B", "A", true),
                                            transition("fromB_toC_destructiveFalse.txt", "B", "C", false),
                                            transition("fromC_toB_destructiveTrue.txt", "C", "B", true))
  }

  describe("ResourceTransitionFinder on an empty resource directory path (defaults to '.')") {
    new TextFixture {
      val testTransitionsResourceDirectory = ""

      it("finds all transitions implemented as resource files") {
        resourceTransitionFinder.getTransitions shouldBe rootLevelTestTransitions
      }
    }
  }
  describe("ResourceTransitionFinder on resource directory path '.'") {
    new TextFixture {
      val testTransitionsResourceDirectory = "."

      it("finds all transitions implemented as resource files") {
        resourceTransitionFinder.getTransitions shouldBe rootLevelTestTransitions
      }
    }
  }
  describe("ResourceTransitionFinder on non-empty resource directory path") {
    new TextFixture {
      val testTransitionsResourceDirectory = "com/foo/bar/test"

      it("finds all transitions implemented as resource files") {
        resourceTransitionFinder.getTransitions shouldBe deepLevelTestTransitions
      }
    }
  }

  describe("ResourceTransitionFinder normalizes resource paths") {

    def resourcesIn(resourceDirPath: String) = new ResourceTransitionFinder(this.getClass.getClassLoader, resourceDirPath, transitionParser, resourceResolvers).getResourcePaths

    val testResourcePaths = Set("com/foo/bar/test/fromA_toB_destructiveFalse.txt",
                                "com/foo/bar/test/fromB_toA_destructiveTrue.txt",
                                "com/foo/bar/test/fromB_toC_destructiveFalse.txt",
                                "com/foo/bar/test/fromC_toB_destructiveTrue.txt")
    Iterable(
      "/com/foo/bar/test",
      "/////com/foo/bar/test"
    ) foreach { path =>
      it(s"with excess leading path separators: '$path'") {
        resourcesIn(path) shouldBe testResourcePaths
      }
    }

    Iterable(
      "com/foo/bar/test/",
      "com/foo/bar/test/////"
    ) foreach { path =>
      it(s"with excess ending path separators: '$path'") {
        resourcesIn(path) shouldBe testResourcePaths
      }
    }

    Iterable(
      "/com//foo////bar/////test////////",
      "com/foo/../foo/bar/test",
      "com/foo/bar/../../foo/bar/test",
      "com/bogus/../foo/fake/drivel/../../bar//test",
      "././com/foo/././bar//test",
      "/./com/foo/././bar//test"
    ) foreach { path =>
      it(s"with excess internal separators and '..' and '.' segments: '$path'") {
        resourcesIn(path) shouldBe testResourcePaths
      }
    }
  }
}
