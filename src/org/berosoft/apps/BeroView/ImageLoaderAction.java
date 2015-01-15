/**
 * 
 */
package org.berosoft.apps.BeroView;

import java.awt.Image;

import javax.swing.ImageIcon;

/**
 * @author rodebe1
 *
 */
public class ImageLoaderAction {
	public interface ImageLoaderEvents {
		void onImageLoadCompleted(String imagePath, Image loadedImage);
	}

	private boolean mIsRunning = false;

	public void run(String imagePath, ImageLoaderEvents callbacks) {
		System.out.println("Started loading image '" + imagePath + "'...");
		Image loadedImage = new ImageIcon(imagePath).getImage();
		completed();
		callbacks.onImageLoadCompleted(imagePath, loadedImage);
		System.out.println("Displaying      image '" + imagePath + "' done...");
	}

	public synchronized void start() {
		mIsRunning = true;
	}

	public synchronized void completed() {
		mIsRunning = false;
	}

	public synchronized boolean isRunning() {
		return mIsRunning;
	}
}
