/**
 * 
 */
package org.berosoft.apps.BeroView;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

/**
 * @author berosoft
 *
 */
public class ImageArea extends JPanel {

	private static final long serialVersionUID = -1217789365570826814L;
	
	private Image currentImage;
	private String currentFilePath;
	private String currentFilePosition;

    public void setImage(Image image) {
    	if(image == null)
    	{
    		currentImage = null;
    		return;
    	}
    	
    	currentImage = image;
    	this.repaint();
    }

	public void setFilePath(String filePath, String filePosition) {
		currentFilePath = filePath;
		currentFilePosition = filePosition;
		this.repaint();
	}
    
    // Class constructor  
    ImageArea() { 
    } 
 
	public void paint(Graphics g) { 
    	super.paint(g);
        if (currentImage != null) {
        	int imageWidth = currentImage.getWidth(this);
        	int imageHeight = currentImage.getHeight(this);
        	int drawAreaWidth = this.getWidth();
        	int drawAreaHeight = this.getHeight();
        	
        	if(imageWidth > drawAreaWidth || imageHeight > drawAreaHeight)
        	{
            	int scaleToWidth = -1;
            	int scaleToHeight = -1;
            	
            	float widthRatio = (float)(imageWidth) / (float)(drawAreaWidth);
            	float heightRatio = (float)(imageHeight) / (float)(drawAreaHeight);
            	if(widthRatio > heightRatio)
            	{
            		scaleToWidth = drawAreaWidth;
            		scaleToHeight = (int)((float)(imageHeight) / widthRatio);
            	}
            	else
            	{
            		scaleToHeight = drawAreaHeight;
            		scaleToWidth = (int)((float)(imageWidth) / heightRatio);
            	}
            	
        		g.drawImage(currentImage, drawAreaWidth / 2 - scaleToWidth / 2, drawAreaHeight / 2 - scaleToHeight / 2, scaleToWidth, scaleToHeight, this);
        	}
        	else
        	{
        		g.drawImage(currentImage, drawAreaWidth / 2 - imageWidth / 2, drawAreaHeight / 2 - imageHeight / 2, this);
        	}
        	
    		

        }
        
    	if(currentFilePath != null && !currentFilePath.isEmpty()) {
    		g.setColor(Color.WHITE);
    		g.drawString(currentFilePath, 25, 25);
    		g.drawString(currentFilePosition, 25, 40);
    	}
    }
}
