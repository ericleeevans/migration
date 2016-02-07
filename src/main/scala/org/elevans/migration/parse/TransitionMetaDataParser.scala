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

import org.elevans.migration.Transition

/**
 * Implementation of the `Transition` trait.
 *
 * @author Eric Evans
 */
case class TransitionMetaData(beforeState: String, afterState: String, isDestructive: Boolean) extends Transition


/**
 * Parse an optional `TransitionMetaData` instance from text.
 *
 * @author Eric Evans
 */
trait TransitionMetadataParser {

  /** Parse an optional `TransitionMetaData` instance from the given String. */
  def parse(s: String): Option[TransitionMetaData]
}

