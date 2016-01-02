package org.elevans.migration.parse

import scala.util.matching.Regex

/**
 * A `TransitionMetadataParser` for text in which the `TransitionMetadata` fields are provided in a key/value format.
 *
 * The keys are beforeStateKey, afterStateKey, and isDestructiveKey, and they must appear in that order in the parsed
 * text. By default, the keys are <em>case-insensitive</em>; this can be explicitly set using caseSensitiveKeys.
 *
 * Each key and its value may be separated by an optional token (keyValueSeparator), which may itself be surrounded by
 * zero or more whitespace characters (spaces, tabs, newlines, etc.). The keyValueSeparator defaults to "".
 *
 * The value for the before state and the after state in the parsed text may contain any non-whitespace characters, and
 * it must be separated from the subsequent text by at least one whitespace character (i.e., the value is "end-delimited"
 * by whitespace). The value for the 'is destructive' flag must be either "true" or "false", and is <em>case-insensitive</em>.
 *
 * For example: If instantiated with beforeStateKey = "transition-before", afterStateKey = "transition-after",
 * isDestructiveKey = "transition-destructive", and keyValueSeparator = "=", all of the following Strings will parse
 * to a TransitionMetaData with beforeState="1.0.4", afterState="1.0.5", and isDestructive=true:
 *  <pre>
 *  "transition-before = 1.0.4 transition-after = 1.0.5 transition-destructive = true"
 *  "transition-before=1.0.4 ; transition-after=1.0.5 ; transition-destructive=true"
 *  "Transition-Before = 1.0.4  Transition-After = 1.0.5   Transition-Destructive = True"
 *  "TRANSITION-BEFORE = 1.0.4 TRANSITION-AFTER = 1.0.5 TRANSITION-DESTRUCTIVE = TRUE"
 *  </pre>
 * Since newlines are ignored as whitespace, the following block parsed as a String also returns the same TransitionMetaData as above:
 *  <pre>
 *  -- transition-before = 1.0.4
 *  -- transition-after = 1.0.5
 *  -- transition-destructive = true
 *  </pre>
 *  as does this block:
 *  <pre>
 *     transition-before =
 *     1.0.4
 *     transition-after
 *       = 1.0.5
 *
 *     transition-destructive
 *     =
 *
 *     true
 *  </pre>
 *
 * @param beforeStateKey    key for the TransitionMetaData beforeState
 * @param afterStateKey     key for the TransitionMetaData afterState
 * @param isDestructiveKey  key for the TransitionMetaData isDestructive flag
 * @param keyValueSeparator separator between each key and value (defaults to "")
 * @param caseSensitiveKeys set to true if keys should be case-sensitive (defaults to false)
 *
 * @author Eric Evans
*/
class KeyValueTransitionMetadataParser(val beforeStateKey: String,
                                       val afterStateKey: String,
                                       val isDestructiveKey: String,
                                       val keyValueSeparator: String = "",
                                       val caseSensitiveKeys: Boolean = false)
  extends RegexTransitionMetadataParser (
    new Regex("""(?""" + (if (caseSensitiveKeys) "" else "i") + """s).*\s*"""
            + beforeStateKey + """\s*""" + keyValueSeparator + """\s*(\S+)\s+.*"""
            + afterStateKey + """\s*""" + keyValueSeparator + """\s*(\S+)\s+.*"""
            + isDestructiveKey + """\s*""" + keyValueSeparator + """\s*(?i)(true|false)\s*.*""")
)
