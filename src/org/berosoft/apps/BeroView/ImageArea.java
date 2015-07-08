/**
 * 
 */
package org.berosoft.apps.BeroView;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

/**
 * @author berosoft
 *
 */
public class ImageArea extends JPanel {

	private static final long serialVersionUID = -1217789365570826814L;
	
	private final float epsilon = 0.00000001f;
	
	private final float zoomStep = 0.8f;
	private float       zoomFactor;
	
	private final int panStep = 25;
	private int       panX;
	private int       panY;
	
	private Image  currentImage;
	private String currentFilePath;
	private String currentFilePosition;
	
    // Class constructor  
	public ImageArea() {
		panX = 0;
		panY = 0;
		zoomFactor = 1.0f;
		setOpaque(true);
		setBackground(Color.BLACK); 
	}

    public void setImage(Image image) {
    	if(image == null)
    	{
    		currentImage = null;
    		return;
    	}
    	
    	zoomFactor = 1.0f;
    	panX = panY = 0;
    	currentImage = image;
    	this.repaint();
    }

	public void setFilePath(String filePath, String filePosition) {
		currentFilePath = filePath;
		currentFilePosition = filePosition;
	}
	
	public void zoomIn() {
		zoomFactor /= zoomStep;
		this.repaint();
	}
	
	public void zoomOut() {
		zoomFactor *= zoomStep;
		this.repaint();
	}
	
	public void panUp() {
		panY -= panStep;
		this.repaint();
	}
    
	public void panDown() {
		panY += panStep;
		this.repaint();
	}
    
	public void panLeft() {
		panX -= panStep;
		this.repaint();
	}
    
	public void panRight() {
		panX += panStep;
		this.repaint();
	}
    
	public void paint(Graphics g) { 
    	super.paint(g);
        if (currentImage != null) {
        	int imageWidth = currentImage.getWidth(this);
        	int imageHeight = currentImage.getHeight(this);
        	int drawAreaWidth = this.getWidth();
        	int drawAreaHeight = this.getHeight();

        	if (Math.abs(zoomFactor - 1.0f) < epsilon) {
        		// 'shrink-to-fit' large images when first displayed
            	if (imageWidth > drawAreaWidth || imageHeight > drawAreaHeight) {
                	float widthRatio = (float)(imageWidth) / (float)(drawAreaWidth);
                	float heightRatio = (float)(imageHeight) / (float)(drawAreaHeight);
                	if (widthRatio > heightRatio) {
                		zoomFactor = 1.0f / widthRatio;
                	} else {
                		zoomFactor = 1.0f / heightRatio;
                	}
            	}
        	}

    		int sourceLeft, sourceTop, sourceRight, sourceBottom;
    		int destinationLeft, destinationTop, destinationRight, destinationBottom;
    		
    		// setup source rectangle -> just the image dimensions
    		sourceLeft = 0; sourceTop = 0;
    		sourceRight = imageWidth; sourceBottom = imageHeight;
    		destinationLeft = drawAreaWidth / 2 - (int)(imageWidth * zoomFactor / 2);
    		destinationRight = drawAreaWidth - destinationLeft;
    		destinationTop = drawAreaHeight / 2 - (int)(imageHeight * zoomFactor / 2);
    		destinationBottom = drawAreaHeight - destinationTop;
    		
       		g.drawImage(currentImage, destinationLeft - panX, destinationTop - panY, destinationRight - panX, destinationBottom - panY,
       				    sourceLeft, sourceTop, sourceRight, sourceBottom, Color.WHITE, this);
        }
        
    	if(currentFilePath != null && !currentFilePath.isEmpty()) {
    		Font currentFont = g.getFont();
    		Font newFont = currentFont.deriveFont(currentFont.getSize() * 1.4f);
    		g.setFont(newFont);
    		g.setColor(Color.WHITE);
    		g.drawString(currentFilePath, 25, 25);
    		g.drawString(currentFilePosition, 25, 40);
    	}
    }
}
