package org.berosoft.apps.BeroView;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.util.*;

import javax.swing.*;

import org.jutils.ui.*;

/**
 * @author berosoft
 *
 */
public class BeroViewFrame extends UFrame {

	private enum Progress
	{
		First,
		Last,
		Next,
		Previous,
		Random,
		NextByBatch,
		PreviousByBatch
	}
	
	private static final long serialVersionUID = -7275993466219595608L;
	
	private ImageArea imageArea;
	private LinkedList<String> bitmapPathList;
	private Image currentImage;
	private int currentIndex;
	private boolean isFullscreen = false;
	private Random random = new Random();
	private boolean showFilePath = false;
	private ImageLoaderAction imageLoaderAction = new ImageLoaderAction();
	
	public BeroViewFrame(LinkedList<String> filePathList, int index) {
		currentIndex = index;
		bitmapPathList = filePathList;
		
		String currentPath = bitmapPathList.get(currentIndex);
		currentImage = new ImageIcon(currentPath).getImage();
			
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		imageArea = new ImageArea();
		imageArea.setImage(currentImage);
		Dimension imageSize = new Dimension(currentImage.getWidth(this), currentImage.getHeight(this));
		imageArea.setMinimumSize(imageSize);
		imageArea.setMaximumSize(imageSize);
		imageArea.setPreferredSize(imageSize);
		getContentPane().add(imageArea);
		pack();
		
        // window should be visible 
        setVisible(true);
        setTitle("BeroView");
        
        addKeyListener(new BeroViewFrameKeyHandler(this));
        addMouseListener(new BeroViewFrameMouseHandler(this));
        addMouseWheelListener(new BeroViewFrameMouseWheelHandler(this));
 	}
	
	public void onKeyPressed(KeyEvent e)
	{
		int keyCode = e.getKeyCode();
		switch(keyCode)
		{
			case KeyEvent.VK_HOME:
				{ 					
					proceedToImage(Progress.First);
				}
				break;
			
			case KeyEvent.VK_END:
				{
					proceedToImage(Progress.Last);
				}
				break;
			
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_KP_LEFT:
			case KeyEvent.VK_BACK_SPACE:
				{ 					
					proceedToImage(Progress.Previous);
				}
				break;
				
			case KeyEvent.VK_PAGE_UP:
			{					
				proceedToImage(Progress.PreviousByBatch);
			}
			break;
				
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_KP_RIGHT:
			case KeyEvent.VK_SPACE:
				{					
					proceedToImage(Progress.Next);
				}
				break;
				
			case KeyEvent.VK_PAGE_DOWN:
			{					
				proceedToImage(Progress.NextByBatch);
			}
			break;
				
			case KeyEvent.VK_Z:
				{
					proceedToImage(Progress.Random);
				}
			break;
			
			case KeyEvent.VK_D:
				{
					showFilePath = !showFilePath;
					updateImageArea(bitmapPathList.get(currentIndex));
				}
				break;
			
			case KeyEvent.VK_F:
			case KeyEvent.VK_V:
			case KeyEvent.VK_ENTER:
				{ // switch between full screen and windowed mode
					toggleFullscreenDisplay();
					updateImageArea(bitmapPathList.get(currentIndex));
				}
				break;
				
			case KeyEvent.VK_ESCAPE:
			case KeyEvent.VK_Q:
				{ // close the BeroViewFrame instance
					this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
				}
				break;

			default:
				break;
		}
	}
	
	public void onMouseClicked(MouseEvent e)
	{
        // System.out.println(e.toString());
	}
	
	public void onMouseWheelMoved(MouseWheelEvent e)
	{
		if(e.isControlDown())
		{ // Control + MouseWheel is progressing randomly
			proceedToImage(Progress.Random);
			return;
		}
		
        int notches = e.getWheelRotation();
        boolean proceedByBatch = e.isShiftDown();
        if (notches < 0) {
        	if(proceedByBatch) {
    			proceedToImage(Progress.PreviousByBatch);
        	} else {
    			proceedToImage(Progress.Previous);
        	}
        } else {
        	if(proceedByBatch) {
    			proceedToImage(Progress.NextByBatch);
        	} else {
    			proceedToImage(Progress.Next);
        	}
        }
	}
	
	private void LoadImageAsync()
	{
		String bitmapPath = bitmapPathList.get(currentIndex);
		imageLoaderAction.start();
		BeroViewFrame imageFrame = this;
        Thread thread = new Thread(new Runnable() {
            public void run() {
                imageLoaderAction.run(bitmapPath, new ImageLoaderAction.ImageLoaderEvents() {
                    @Override
                    public void onImageLoadCompleted(String imagePath, Image loadedImage) {
                    	imageFrame.currentImage = loadedImage;
                    	updateImageArea(bitmapPath);
                    }
                });
            }
        });
        thread.start();
	}
	
	private void updateImageArea(String bitmapPath)
	{
		if(!isFullscreen)
		{
			Dimension imageSize = new Dimension(currentImage.getWidth(this), currentImage.getHeight(this));
			if(!imageSize.equals(imageArea.getPreferredSize()))
			{
				imageArea.setMinimumSize(imageSize);
				imageArea.setMaximumSize(imageSize);
				imageArea.setPreferredSize(imageSize);
				pack();
			}
			
			imageArea.setFilePath("");
			
			if(showFilePath) {
		        setTitle("BeroView - " + bitmapPath);
			}
			else {
				setTitle("BeroView");
			}
		}
		else {
			if(showFilePath) {
				imageArea.setFilePath(bitmapPath);			
			}
			else {
				imageArea.setFilePath("");
			}
		}
		imageArea.setImage(currentImage);
	}
	
	private void proceedToImage(Progress progress)
	{
		if(imageLoaderAction.isRunning())
		{
	    	System.out.println("Load in progress. Request discarded: " + progress.toString());
			return;
		}
		
		switch(progress)
		{
			case First:
				{ // show the first image from the bitmap file list
					currentIndex = 0;
				}
				break;
			case Last:
				{ // show the first image from the bitmap file list
					currentIndex = bitmapPathList.size() - 1;
				}
				break;
			case Next:
				{ // show the next image from the bitmap file list or 'wrap around' when currently showing the last image
					if(currentIndex == bitmapPathList.size() - 1)
					{
						currentIndex = 0;
					}
					else
					{
						currentIndex++;
					}
				}
				break;
			case NextByBatch:
				{
					int batch = bitmapPathList.size() / 10;
					currentIndex += batch;
					if(currentIndex >= bitmapPathList.size())
					{
						currentIndex -= bitmapPathList.size();
					}
				}
				break;
			case Previous:
				{ // show the previous image from the bitmap file list or 'wrap around' when currently showing the first image
					if(currentIndex == 0)
					{
						currentIndex = bitmapPathList.size() - 1;
					}
					else
					{
						currentIndex--;
					}
				}
				break;
			case PreviousByBatch:
				{
					int batch = bitmapPathList.size() / 10;
					currentIndex -= batch;
					if(currentIndex < 0)
					{
						currentIndex += bitmapPathList.size();
					}
				}
				break;
			case Random:
				{ // show a randomly selected image from the list of bitmap files
					currentIndex = random.nextInt(bitmapPathList.size());
				}
				break;
			default:
				break;
		}
		
		LoadImageAsync();
	}
	
	private void toggleFullscreenDisplay()
	{
		// switch to new state
		isFullscreen = !isFullscreen;
		GraphicsDevice screenDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
		if(isFullscreen)
		{ // switch mode to full screen display
			dispose();
			setUndecorated(true);
			screenDevice.setFullScreenWindow(this);
			setVisible(true);
		}
		else
		{ // switch mode to windowed display
			dispose();
			screenDevice.setFullScreenWindow(null);
			setUndecorated(false);
			setVisible(true);
		}
	}
}

class BeroViewFrameKeyHandler extends KeyAdapter
{
	private BeroViewFrame adaptee;
	
	public BeroViewFrameKeyHandler( BeroViewFrame adaptee )
	{
		this.adaptee = adaptee;
	}
	
    public void keyPressed(KeyEvent e)
    {
    	adaptee.onKeyPressed(e);
    }
}

class BeroViewFrameMouseHandler extends MouseAdapter
{
	private BeroViewFrame adaptee;
	
	public BeroViewFrameMouseHandler( BeroViewFrame adaptee )
	{
		this.adaptee = adaptee;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		this.adaptee.onMouseClicked(e);
	}
}

class BeroViewFrameMouseWheelHandler implements MouseWheelListener
{
	private BeroViewFrame adaptee;
	
	public BeroViewFrameMouseWheelHandler( BeroViewFrame adaptee )
	{
		this.adaptee = adaptee;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		adaptee.onMouseWheelMoved(e);
	}
}
