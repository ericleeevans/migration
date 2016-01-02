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

package org.elevans.migration.parse

import scala.util.matching.Regex

/**
 * `TransitionMetadataParser` implementation that can use any `Regex` that matches the three components of `TransitionMetaData`.
 *
 * @author Eric Evans
 */
class RegexTransitionMetadataParser(val regex: Regex) extends TransitionMetadataParser {

  def parse(s: String) = s match {
    case regex(before, after, isDestructive) if isBoolean(isDestructive) => Some(TransitionMetaData(before, after, isDestructive.toBoolean))
    case _ => None
  }

  private[this] def isBoolean(s:String) = scala.util.Try{s.toLowerCase.toBoolean; true}.getOrElse(false)
}
