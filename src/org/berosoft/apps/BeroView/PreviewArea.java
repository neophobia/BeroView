package org.berosoft.apps.BeroView;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;


/**
 * @author berosoft
 *
 */
public class PreviewArea extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 674836579694610055L;

    Image scaledImage; // down loaded image  
 
    public void setPreviewImage(Image image) {
    	if(image == null)
    	{
    		scaledImage = null;
    		return;
    	}
    	
    	scaledImage = scaleImage(image);
    	this.repaint();
    }
    
    // Class constructor  
    PreviewArea() { 
    } 
 
	public void paint(Graphics g) { 
    	super.paint(g);
        if (scaledImage != null) {
        	// TODO: draw the image centered
            g.drawImage(scaledImage, this.getWidth() / 2 - scaledImage.getWidth(this) / 2, this.getHeight() / 2 - scaledImage.getHeight(this) / 2, this);  
        }
    } 
	
	private Image scaleImage( Image rawImage ) {  
		Image scaledImage = null;  
		try {  
			int rawImageWidth = rawImage.getWidth( this );  
			int rawImageHeight = rawImage.getHeight( this );  
			int paneWidth = (int) getWidth();  
			int paneHeight = (int) getHeight();  
			float widthRatio = (float) rawImageWidth / (float) paneWidth;  
			float heightRatio = (float) rawImageHeight / (float) paneHeight;  
			int widthFactor = -1;  
			int heightFactor = -1;  
			if ( ( widthRatio > heightRatio ) && ( widthRatio > 1.0 ) )  
				widthFactor = paneWidth;  
			else if ( ( heightRatio > widthRatio ) && ( heightRatio > 1.0 ) )  
				heightFactor = paneHeight;  
			if ( ( widthFactor < 0 ) && ( heightFactor < 0 ) )  
				scaledImage = rawImage;  
			else   
				scaledImage = rawImage.getScaledInstance( widthFactor, heightFactor, Image.SCALE_SMOOTH );  
		} catch( Exception e ) {  
			e.printStackTrace();  
		}  
		return( scaledImage );  
	}
}