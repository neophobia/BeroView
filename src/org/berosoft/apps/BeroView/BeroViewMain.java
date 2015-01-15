package org.berosoft.apps.BeroView;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jutils.ui.FrameRunner;

/**
 * @author berosoft
 *
 */
public class BeroViewMain extends FrameRunner
{
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater( new BeroViewMain() );
	}

	@Override
	protected JFrame createFrame() {
       BeroViewExplorer frame = new BeroViewExplorer();
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setDirectory( new File( "/" ) );

        return frame;
	}

	@Override
	protected boolean validate() {
		return false;
	}
}
