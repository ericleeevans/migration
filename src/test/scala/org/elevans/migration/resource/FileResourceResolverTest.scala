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
