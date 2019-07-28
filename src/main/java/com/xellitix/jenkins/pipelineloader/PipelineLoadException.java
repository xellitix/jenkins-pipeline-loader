package com.xellitix.jenkins.pipelineloader;

/**
 * {@link PipelineLoader} exception.
 *
 * @author Grayson Kuhns
 */
public class PipelineLoadException extends RuntimeException {

  // Constants
  private static final String MSG_TMPL =
      "Failed to load pipeline \"%s\" from DSL resource \"%s\"";

  /**
   * Constructor.
   *
   * @param pipelineName The pipeline name.
   * @param dslPath The path to the pipeline DSL.
   * @param cause The cause of the failure.
   */
  public PipelineLoadException(
      final String pipelineName,
      final String dslPath,
      final Throwable cause) {

    super(
        String.format(MSG_TMPL, pipelineName, dslPath),
        cause);
  }

  /**
   * Constructor.
   *
   * @param pipelineName The pipeline name.
   * @param dslPath The path to the pipeline DSL.
   */
  public PipelineLoadException(
      final String pipelineName,
      final String dslPath) {

    super(String.format(MSG_TMPL, pipelineName, dslPath));
  }
}
