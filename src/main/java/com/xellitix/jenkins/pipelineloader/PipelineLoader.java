package com.xellitix.jenkins.pipelineloader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;

import groovy.lang.GroovyCodeSource;
import org.jenkinsci.plugins.workflow.cps.CpsThread;

/**
 * Jenkins pipeline loader.
 *
 * <p>
 *   Original:
 *   https://github.com/jenkinsci/simple-build-for-pipeline-plugin
 * </p>
 *
 * @author Grayson Kuhns
 */
public class PipelineLoader extends GlobalVariable {

  // Properties
  private final String pipelineName;
  private final String dslPath;

  /**
   * Constructor.
   *
   * @param pipelineName The name of the pipeline.
   * @param dslPath The path to the pipeline DSL.
   */
  public PipelineLoader(
      final String pipelineName,
      final String dslPath) {

    this.pipelineName = pipelineName;
    this.dslPath = dslPath;
  }

  /**
   * Gets the pipeline name.
   *
   * @return The pipeline name.
   */
  @Nonnull
  @Override
  public String getName() {
    return pipelineName;
  }

  /**
   * Loads the pipeline.
   *
   * @param cpsScript The {@link CpsScript} the pipeline is being loaded into.
   *
   * @return The pipeline.
   * @throws PipelineLoadException If an error occurs while loading the pipeline.
   */
  @Nonnull
  @Override
  public Object getValue(@Nonnull final CpsScript cpsScript) throws PipelineLoadException {
    // Get the current CpsThread
    final CpsThread cpsThread = CpsThread.current();
    if (cpsThread == null) {
      throw new PipelineLoadException(
          pipelineName,
          dslPath,
          new IllegalStateException("Expected to be called from a CpsThread"));
    }

    // Load the pipeline code
    final URL dslResource = getClass()
        .getClassLoader()
        .getResource(dslPath);
    if (dslResource == null) {
      throw new PipelineLoadException(
          pipelineName,
          dslPath,
          new IllegalStateException("Expected the DSL resource to exist"));
    }

    Reader dslReader;
    try {
      dslReader = new InputStreamReader(
          dslResource.openStream(),
          StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new PipelineLoadException(pipelineName, dslPath, ex);
    }

    final GroovyCodeSource dsl = new GroovyCodeSource(
        dslReader,
        pipelineName.concat(".groovy"),
        dslResource.getFile());

    dsl.setCachable(true);

    // Instantiate the pipeline
    Object pipeline;

    try {
      pipeline = cpsThread
          .getExecution()
          .getShell()
          .getClassLoader()
          .parseClass(dsl)
          .newInstance();
    } catch (IllegalAccessException | InstantiationException ex) {
      throw new PipelineLoadException(pipelineName, dslPath, ex);
    }

    // Bind the pipeline into the groovy runtime
    cpsScript
        .getBinding()
        .setVariable(pipelineName, pipeline);
    return pipeline;
  }
}
