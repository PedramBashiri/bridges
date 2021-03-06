package edu.uncc.cs.bridges;

/**
 * Visualizer interface to be implemented by
 * GraphVisualizer, StackVisualizer, perhaps more.
 * @author Sean Gallagher
 *
 */
public abstract class Visualizer {
	/** 
	 * JSON exporting interface to be called internally by Bridges
	 * @returns a complete, valid JSON object (not array) for uploading
	 */
	protected abstract String getRepresentation();
}
