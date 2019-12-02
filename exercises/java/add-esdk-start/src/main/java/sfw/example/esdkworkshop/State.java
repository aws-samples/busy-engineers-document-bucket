// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package sfw.example.esdkworkshop;

import com.moandjiezana.toml.Toml;
import java.io.File;

// For automatic mapping, these classes all have names dictated by the TOML file.
// CHECKSTYLE:OFF MemberName
// CHECKSTYLE:OFF ParameterName
// CHECKSTYLE:OFF AbbreviationAsWordInName

/**
 * Helper to pull configuration of CloudFormation-managed AWS resources out of the generated state
 * file.
 */
public class State {
  public final Contents contents;

  /**
   * Parse the state file at the provided path.
   *
   * @param path the path to the state file.
   */
  public State(String path) {
    // Java does not expand ~ automatically
    String canonicalizedPath = path.replaceFirst("~", System.getProperty("user.home"));
    contents = new Toml().read(new File(canonicalizedPath)).to(Contents.class);
  }

  /** The top-level content structure of the state file. */
  public static class Contents {
    /** The name of the Document Bucket S3 bucket. */
    public final String DocumentBucket;
    /** The name of the Document Bucket DynamoDB table. */
    public final String DocumentTable;
    /** The ARN of Faythe's CMK. */
    public final String FaytheCMK;
    /** The ARN of Walter's CMK. */
    public final String WalterCMK;

    Contents(String DocumentBucket, String DocumentTable, String FaytheCMK, String WalterCMK) {
      this.DocumentBucket = DocumentBucket;
      this.DocumentTable = DocumentTable;
      this.FaytheCMK = FaytheCMK;
      this.WalterCMK = WalterCMK;
    }
  }
}
