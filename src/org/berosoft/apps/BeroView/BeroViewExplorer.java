package org.berosoft.apps.BeroView;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreeSelectionModel;

import org.jutils.IconConstants;
import org.jutils.Utils;
import org.jutils.io.FileComparator;
import org.jutils.ui.*;
import org.jutils.ui.explorer.*;

/*******************************************************************************
 * Frame that displays the contents of the file system in a explorer like
 * interface.
 ******************************************************************************/
public class BeroViewExplorer extends UFrame
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 2620698800871758333L;

	/**  */
    private static final String FORWARD_TIP = "You must go back before going forward";

    /**  */
    private static final String BACKWARD_TIP = "You must go somewhere in order to go back";

    // -------------------------------------------------------------------------
    // Menu bar widgets
    // -------------------------------------------------------------------------
    /** The main menu bar for this frame. */
    private JMenuBar menubar = new UMenuBar();

    /** Menus containing items normally found under the name 'File'. */
    private JMenu fileMenu = new JMenu();

    /** Allows the user to exit this application. */
    private JMenuItem exitMenuItem = new JMenuItem();

    /** Allows the user to save & clear the marked images list. */
    private JMenuItem saveMarkedImagesListMenuItem = new JMenuItem();

    /** Menu that displays the tools of this application. */
    private JMenu toolsMenu = new JMenu();

    /** Allows the user to view and edit the options of this application. */
    private JMenuItem optionsMenuItem = new JMenuItem();
    
    private JPopupMenu contextMenu = new JPopupMenu();

    // -------------------------------------------------------------------------
    // Toolbar widgets
    // -------------------------------------------------------------------------
    /** The main toolbar for this frame. */
    private JToolBar toolbar = new UToolBar();

    /** Allows the user to navigate to past shown directories. */
    private JButton backButton = new JButton();

    /**
     * Allows the user to navigate to future shown directories (only works if
     * the user has previously navigated backwards).
     */
    private JButton nextButton = new JButton();

    /** Allows the user to navigate to the current directories parent. */
    private JButton upButton = new JButton();

    /**
     * Allows the user to refresh the current directory in both the file table
     * and file tree.
     */
    private JButton refreshButton = new JButton();

    // -------------------------------------------------------------------------
    // Address panel.
    // -------------------------------------------------------------------------
    /** The address panel. */
    private JPanel addressPanel = new JPanel();

    /** The label for the address field. */
    private JLabel addressLabel = new JLabel();

    /** The text field containing the path of the current directory. */
    private JTextField addressField = new JTextField();

    // -------------------------------------------------------------------------
    // Main panel widgets
    // -------------------------------------------------------------------------
    /** The main panel of this frame. */
    private JPanel mainPanel = new JPanel();

    /** The file tree displaying the directories in the given file system. */
    private DirTree dirTree = new DirTree();

    /** The scroll pane for the file tree. */
    private JScrollPane treeScrollPane = new JScrollPane( dirTree );
    
    /** The preview frame. */
    private PreviewArea previewArea = new PreviewArea();
    
    /**
     * The split pane containing the file tree on the top and the preview area on
     * the bottom.
     */
    private JSplitPane topBottomSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    /**
     * The file table displaying all the files and folder for the current
     * directory.
     */
    private ExplorerTable fileTable = new ExplorerTable();

    /** The scroll pane for the file table. */
    private JScrollPane tableScrollPane = new JScrollPane( fileTable );

    /**
     * The split pane containing the topBottomSplitPane on the left and the file table on
     * the right.
     */
    private JSplitPane leftRightSplitPane = new JSplitPane();

    /** The panel at the bottom of the frame. */
    private StatusBarPanel statusPanel = new StatusBarPanel();

    // -------------------------------------------------------------------------
    // Supporting data.
    // -------------------------------------------------------------------------
    /** The directory currently displayed. */
    private File currentDirectory = null;
    
    private LinkedList<File> currentFileList = new LinkedList<File>(); 

    /** The directory currently displayed. */
    private File lastDirectory = null;
    
    private File slideshowStartDirectory = null;

    /**
     * The list of directories backward (in history) from the current directory.
     */
    private LinkedList<File> lastDirs = new LinkedList<File>();

    /** The list of directories forward (in history) from the current directory. */
    private LinkedList<File> nextDirs = new LinkedList<File>();
    
    private LinkedList<String> markedImagesList = new LinkedList<String>();

    /***************************************************************************
     * Creates a BeroViewExplorer frame.
     **************************************************************************/
    public BeroViewExplorer()
    {
        ActionListener upButtonListener = new UpButtonActionHandler( this );
        ActionListener nextButtonListener = new NextButtonActionHandler( this );
        ActionListener backButtonListener = new BackButtonActionHandler( this );
        ActionListener optionsMenuItemListener = new OptionMenuItemActionHandler( this );
        ActionListener addressFieldListener = new AddressFieldActionHandler( this );
        TreeSelectionListener dirTreeSelListener = new DirTreeSelectionHandler( this );
        MouseListener dirTreeMouseListener = new DirTreeMouseHandler( this );
        MouseListener fileTableMouseListener = new FileTableMouseHandler( this );
        ListSelectionListener fileTableSelListener = new FileTableSelectionHandler( this );
        KeyListener fileTableKeyListener = new FileTableKeyHandler( this );
        SaveMarkedImagesMenuListener saveMarkedImagesMenuListener = new SaveMarkedImagesMenuListener( this );

        // ----------------------------------------------------------------------
        // Setup menu bar
        // ----------------------------------------------------------------------
        fileMenu.setText( "File" );

        exitMenuItem.setText( "Exit" );
        exitMenuItem.addActionListener( new ExitListener() );
        exitMenuItem.setIcon( IconConstants.getIcon( IconConstants.CLOSE_16 ) );
        fileMenu.add( exitMenuItem );
        
        saveMarkedImagesListMenuItem.setText("Save & clear marked images list");
        saveMarkedImagesListMenuItem.addActionListener( saveMarkedImagesMenuListener );
        saveMarkedImagesListMenuItem.setIcon( IconConstants.getIcon( IconConstants.SAVE_16));
        saveMarkedImagesListMenuItem.setEnabled(false);
        fileMenu.add( saveMarkedImagesListMenuItem );

        toolsMenu.setText( "Tools" );
        optionsMenuItem.setText( "Options" );
        optionsMenuItem.setIcon( IconConstants.getIcon( IconConstants.CONFIG_16 ) );
        // optionsMenuItem.setIcon( IconLoader.getIcon( IconLoader.EDIT_16 ) );
        optionsMenuItem.addActionListener( optionsMenuItemListener );
        toolsMenu.add( optionsMenuItem );

        menubar.add( fileMenu );
        menubar.add( toolsMenu );

        this.setJMenuBar( menubar );
        
        final BeroViewExplorer that = this;
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing( java.awt.event.WindowEvent windowEvent ) {
            	final BeroViewExplorer explorer = that;
            	explorer.checkForMarkedImages();
            }
        });

        // ---------------------------------------------------------------------
        // Setup tool bar
        // ---------------------------------------------------------------------
        toolbar.setFloatable( false );
        toolbar.setRollover( true );
        toolbar.setBorderPainted( false );

        backButton.setText( "" );
        backButton.setToolTipText( BACKWARD_TIP );
        backButton.setIcon( IconConstants.getIcon( IconConstants.BACK_24 ) );
        backButton.setFocusable( false );
        backButton.setEnabled( false );
        backButton.addActionListener( backButtonListener );

        nextButton.setText( "" );
        nextButton.setToolTipText( FORWARD_TIP );
        nextButton.setIcon( IconConstants.getIcon( IconConstants.FORWARD_24 ) );
        nextButton.setFocusable( false );
        nextButton.setEnabled( false );
        nextButton.addActionListener( nextButtonListener );

        upButton.setText( "" );
        upButton.setToolTipText( "Go to parent directory" );
        upButton.setIcon( IconConstants.getIcon( IconConstants.UP_24 ) );
        upButton.setFocusable( false );
        upButton.addActionListener( upButtonListener );

        refreshButton.setText( "" );
        refreshButton.setToolTipText( "Refresh the current directory" );
        refreshButton.setIcon( IconConstants.getIcon( IconConstants.REFRESH_24 ) );
        refreshButton.setFocusable( false );

        toolbar.add( backButton );
        toolbar.add( nextButton );
        toolbar.addSeparator();
        toolbar.add( upButton ); 
        toolbar.addSeparator();
        toolbar.add( refreshButton );
        
        // ---------------------------------------------------------------------
        // Setup context menu.
        // ---------------------------------------------------------------------

        
        JMenuItem menuSlideshowRecursively = new JMenuItem("Slideshow recursively", IconConstants.getIcon(IconConstants.CONFIG_16));
        menuSlideshowRecursively.addActionListener(
        		new ActionListener() {
    				public void actionPerformed(ActionEvent e) {
						createSlideshowRecursively(slideshowStartDirectory);
						slideshowStartDirectory = null;
    				}
        
        		});
        contextMenu.add(menuSlideshowRecursively);
        
        // ---------------------------------------------------------------------
        // Setup address panel.
        // ---------------------------------------------------------------------
        addressPanel.setLayout( new GridBagLayout() );

        addressLabel.setText( "Address: " );
        addressField.setText( "" );
        addressField.addActionListener( addressFieldListener );

        addressPanel.add( addressLabel, new GridBagConstraints( 0, 0, 1, 1,
            0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets( 4, 4, 4, 4 ), 0, 0 ) );
        addressPanel.add( addressField, new GridBagConstraints( 1, 0, 1, 1,
            1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets( 4, 4, 4, 4 ), 0, 0 ) );

        // ---------------------------------------------------------------------
        // Setup main panel.
        // ---------------------------------------------------------------------
        mainPanel.setLayout( new GridBagLayout() );

        this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        this.setTitle( "BeroView" );

        dirTree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION );
        dirTree.addTreeSelectionListener( dirTreeSelListener );
        dirTree.addMouseListener( dirTreeMouseListener );

        topBottomSplitPane.setTopComponent(treeScrollPane);
        topBottomSplitPane.setBottomComponent(previewArea);

        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileTable.setAutoCreateRowSorter( true );
        fileTable.setBackground( Color.white );
        fileTable.getSelectionModel().addListSelectionListener(fileTableSelListener);
        fileTable.addMouseListener( fileTableMouseListener );
        fileTable.addKeyListener( fileTableKeyListener );
        
        tableScrollPane.getViewport().setBackground( Color.white );

        leftRightSplitPane.setLeftComponent( topBottomSplitPane );
        leftRightSplitPane.setRightComponent( tableScrollPane );

        mainPanel.add( addressPanel, new GridBagConstraints( 0, 0, 1, 1, 1.0,
            0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets( 0, 0, 0, 0 ), 0, 0 ) );

        mainPanel.add( leftRightSplitPane, new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 4,
                4, 4, 4 ), 0, 0 ) );

        // ---------------------------------------------------------------------
        // Setup frame
        // ---------------------------------------------------------------------
        this.getContentPane().setLayout( new BorderLayout() );

        this.getContentPane().add( toolbar, BorderLayout.NORTH );
        this.getContentPane().add( mainPanel, BorderLayout.CENTER );
        this.getContentPane().add( statusPanel.getView(), BorderLayout.SOUTH );

        setIconImages( IconConstants.getImages( IconConstants.OPEN_FOLDER_16,
            IconConstants.OPEN_FOLDER_32 ) );

        setSize( new Dimension( 600, 450 ) );
        leftRightSplitPane.setDividerLocation( 150 );
    }
    
    /***************************************************************************
     * Sets the directory shown in this frame.
     * @param dir The directory to be shown.
     **************************************************************************/
    public void setDirectory( File dir )
    {
        setDirectory( dir, true );
    }
    
    private void createSlideshowRecursively(File startDir)
    {
        if(startDir != null && startDir.isDirectory())
        {
        	final BeroViewExplorer explorer = this;
        	final LinkedList<String> slideshowFilePaths = new LinkedList<String>();
        	traverseTreeForBitmapFiles(startDir, slideshowFilePaths);
        	
        	if(slideshowFilePaths.size() <= 0)
        	{
        		return;
        	}
        	
			final int currentIndex = 0;
	        EventQueue.invokeLater(new Runnable() {
	            public void run() {
        	        new BeroViewFrame(explorer, slideshowFilePaths, currentIndex);
	            }
	        });
        }
        
    }
    
    private void traverseTreeForBitmapFiles(File dir, LinkedList<String> bitmapPathList)
    {
        File[] children = dir.listFiles();
        LinkedList<File> subDirs = new LinkedList<File>();

        if( children != null )
        {
            for( int i = 0; i < children.length; i++ )
            {
            	if(children[i].isDirectory())
            	{
            		subDirs.add(children[i]);
            	}
            	else if(this.looksLikeBitmapFile(children[i]))
            	{
            		bitmapPathList.add(children[i].getAbsolutePath());
            	}
            }
            
            Iterator<File> iterator = subDirs.listIterator();

            while (iterator.hasNext())
            {
            	traverseTreeForBitmapFiles(iterator.next(), bitmapPathList);
            }        
        }
        else
        {
            JOptionPane.showMessageDialog( this,
                "User does not have permissions to view: " + Utils.NEW_LINE +
                    dir.getAbsolutePath(), "ERROR", JOptionPane.ERROR_MESSAGE );
        }
    }

    /***************************************************************************
     * Sets the current directory either by setting the selected file in the
     * tree (which sets the files in the table) or showing the files contents in
     * the table.
     * @param dir The directory to be shown.
     * @param setTree Displays the folder contents by setting the selected file
     * in the tree (true) or setting the table directly (false).
     **************************************************************************/
    private void setDirectory( File dir, boolean setTree )
    {
        File parent = dir.getParentFile();
        if( currentDirectory != null && !currentDirectory.equals( dir ) )
        {
            lastDirectory = currentDirectory;
        }
        currentDirectory = dir;

        if( dir.isDirectory() )
        {
            if( setTree )
            {
                dirTree.setSelected( new File[] { dir } );
            } else {
                showDirInTable( dir );
            }
            addressField.setText( dir.getAbsolutePath() );
            statusPanel.setText( this.fileTable.getExplorerTableModel().getRowCount() +
                " items." );
        }

        upButton.setEnabled( parent != null );
    }
    
    public LinkedList<String> getMarkedImages() {
		return markedImagesList;
	}

	public void setMarkedImages( LinkedList<String> markedImagesList ) {
		this.markedImagesList = markedImagesList;
	}
	
	public void toggleMarkedState( String fileAbsolutePath ) {
		if ( fileAbsolutePath == null || fileAbsolutePath.length() == 0 ) {
			return;
		}
		
		int index = markedImagesList.indexOf(fileAbsolutePath);
		
		if (index >= 0) {
			markedImagesList.remove(index);
		} else {
			markedImagesList.add( fileAbsolutePath );
		}
		saveMarkedImagesListMenuItem.setEnabled(markedImagesList.size() > 0);
	}

	/***************************************************************************
     * Returns the current directory displayed.
     * @return The current directory displayed.
     **************************************************************************/
    public File getDirectory()
    {
        return currentDirectory;
    }

    /***************************************************************************
     * Returns the value of the address field.
     * @return The value of the address field.
     **************************************************************************/
    public String getAddress()
    {
        return addressField.getText();
    }

    /***************************************************************************
     * Displays the given directory's contents in the table.
     * @param f The file to be displayed.
     **************************************************************************/
    private void showDirInTable( File dir )
    {
        ArrayList<DefaultExplorerItem> list = new ArrayList<DefaultExplorerItem>();
        File[] children = dir.listFiles();

        currentFileList.clear();
        String directoryName = dir.getName();
        String title = "BeroView";
        if(directoryName != null && !directoryName.isEmpty())
        {
        	title += " - " + directoryName;
        }
        this.setTitle(title);

        if( children != null )
        {
            Arrays.sort( children, new FileComparator() );

            for( int i = 0; i < children.length; i++ )
            {
                list.add( new DefaultExplorerItem( children[i] ) );
                currentFileList.add(children[i]);
            }

            fileTable.clearTable();
            fileTable.addFiles( list );
        }
        else
        {
            JOptionPane.showMessageDialog( this,
                "User does not have permissions to view: " + Utils.NEW_LINE +
                    dir.getAbsolutePath(), "ERROR", JOptionPane.ERROR_MESSAGE );
        }
    }

    /***************************************************************************
     * Adds the given file to the history of folders traversed. The file is NOT
     * null checked and is added only if it is not equal to the last file in the
     * history or the current directory.
     * @param file The file to be added.
     **************************************************************************/
    private void addLastFile()
    {
        File lastFile = ( File )lastDirs.peekFirst();

        if( lastDirectory != null )
        {
            if( lastFile == null || !lastFile.equals( lastDirectory ) )
            {
                lastDirs.push( lastDirectory );
            }
            nextDirs.clear();

            nextButton.setEnabled( false );
            backButton.setEnabled( true );
            backButton.setToolTipText( lastDirectory.getAbsolutePath() );
        }
    }
    
    private boolean looksLikeBitmapFile(File file)
    {
    	String[] fileExtensions =  new String[] {"jpg", "jpeg", "png", "gif", "bmp", "tiff"};
    	
        for (String extension : fileExtensions)
        {
          if (file.getName().toLowerCase().endsWith(extension))
          {               
            return true;
          }
        }
        return false;
    }
    
    private boolean looksLikePlaylistFile(File file)
    {
    	String[] fileExtensions =  new String[] {"bvs", "ais"};
    	
        for (String extension : fileExtensions)
        {
          if (file.getName().toLowerCase().endsWith(extension))
          {               
            return true;
          }
        }
        return false;
    }
    
    private void fileTableRowSelected(int selectedIndex)
    {
    	if(selectedIndex < 0)
    	{
    		previewArea.setPreviewImage(null);
    	}
    	else
    	{
    		File file = fileTable.getSelectedFile();
    		if(file.isFile() && looksLikeBitmapFile(file))
    		{
    			Image image = new ImageIcon(file.getAbsolutePath()).getImage();
    			previewArea.setPreviewImage(image);
    		}
    	}
    }
    
    private LinkedList<String> readPlaylistFile(File file)
    {
    	LinkedList<String> content = new LinkedList<String>();
    	
    	try
    	{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null)
			{
				String path = getDirectory() + "/" + line;
			    content.add(path);
			}
			
			br.close();
		}
    	catch (Exception e)
    	{
			e.printStackTrace();
		}
    	
    	return content;
    }
    
    private void fileTableFileOpen() throws Exception
    {
    	final BeroViewExplorer explorer = this;
        File file = fileTable.getSelectedFile();
        if( file != null )
        {
            if( file.isDirectory() )
            {
                // showFile( file );
                setDirectory( file );
                addLastFile();
            }
            else
            {
            	if(this.looksLikeBitmapFile(file))
            	{
            		final LinkedList<String> bitmapPathList = new LinkedList<String>();
            		
            		ExplorerTableModel fileTableModel = (ExplorerTableModel)fileTable.getModel();
            		int itemCount = fileTableModel.getRowCount();
            		for(int i = 0; i < itemCount; i++)
            		{
            			File itemFile = fileTableModel.getExplorerItem(i).getFile();
            			if(itemFile.isDirectory())
            			{
            				continue;
            			}
            			
            			bitmapPathList.add(itemFile.getAbsolutePath());
            		}
            		
            		final int currentIndex = bitmapPathList.indexOf(file.getAbsolutePath());
            		
        	        EventQueue.invokeLater(new Runnable() {
        	            public void run() {
                	        new BeroViewFrame(explorer, bitmapPathList, currentIndex);
        	            }
        	        });
            	}
            	else
            	{
            		if(looksLikePlaylistFile(file))
            		{
            			final LinkedList<String> playlistContent = readPlaylistFile(file);
            			final int currentIndex = 0;
            	        EventQueue.invokeLater(new Runnable() {
            	            public void run() {
                    	        new BeroViewFrame(explorer, playlistContent, currentIndex);
            	            }
            	        });
            		}
            		else
            		{
                        try
                        {
                            Desktop.getDesktop().open( file );
                        }
                        catch( Exception ex )
                        {
                            JOptionPane.showMessageDialog( this, ex.getMessage(),
                                "ERROR", JOptionPane.ERROR_MESSAGE );
                        }
            		}
            	}
            }
        }
    }
    
    private void checkForTriggerContextMenu(MouseEvent e)
    {
        File file = fileTable.getSelectedFile();

    	if(e.isPopupTrigger() && file != null && file.isDirectory())
    	{
    		// save the selection
    		slideshowStartDirectory = file;
            contextMenu.show( e.getComponent(), e.getX(), e.getY() );  
    	}
    }

    /***************************************************************************
     * Callback listener invoked when the mouse has clicked on the file table.
     * @param e The MouseEvent (may NOT be null) that occurred.
     **************************************************************************/
    public void listener_fileTable_mouseClicked( MouseEvent e )
    {
        if( e.getClickCount() == 2 )
        {
        	try {
				fileTableFileOpen();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
        }
    }
    
    /***************************************************************************
     * Callback listener invoked when the mouse has pressed on the file table.
     * @param e The MouseEvent (may NOT be null) that occurred.
     **************************************************************************/
    public void listener_fileTable_mousePressed( MouseEvent e )
    {
    	checkForTriggerContextMenu(e);
    }
    
    /***************************************************************************
     * Callback listener invoked when the mouse was released on the file table.
     * @param e The MouseEvent (may NOT be null) that occurred.
     **************************************************************************/
    public void listener_fileTable_mouseReleased( MouseEvent e )
    {
    	checkForTriggerContextMenu(e);
    }
    
    /***************************************************************************
     * Callback listener invoked when a file table row has been selected or
     * deselected by either the user or programmatically.
     * @param e The ignored (can be null) TreeSelectionEvent that occurred.
     **************************************************************************/
	public void listener_fileTable_valueChanged(ListSelectionEvent e) {
	    if (e.getValueIsAdjusting())
	        return;

	    String strSource= e.getSource().toString();
	    int start = strSource.indexOf("{")+1,
	        stop  = strSource.length()-1;
	    int selectedIndex = -1;
	    
	    if(start < stop)
	    {
	    	selectedIndex = Integer.parseInt(strSource.substring(start, stop));
	    }
	    
	    fileTableRowSelected(selectedIndex);
	}

    /***************************************************************************
     * 
     * @param e The KeyEvent (may NOT be null) that occurred.
     * @throws Exception 
     **************************************************************************/
    public void listener_fileTable_keyPressed( KeyEvent e ) throws Exception
    {
		int keyCode = e.getKeyCode();
		if(keyCode == KeyEvent.VK_ENTER) {
			fileTableFileOpen();
		}
    }
    
    /***************************************************************************
     * Callback listener invoked when the back button is clicked.
     * @param e The ignored (can be null) ActionEvent that occurred.
     **************************************************************************/
    public void listener_backButton_actionPerformed( ActionEvent e )
    {
        File file = ( File )lastDirs.pollFirst();
        if( file != null )
        {
            nextDirs.push( currentDirectory );

            backButton.setEnabled( !lastDirs.isEmpty() );
            nextButton.setEnabled( true );
            nextButton.setToolTipText( currentDirectory.getAbsolutePath() );

            setDirectory( file );

            file = ( File )lastDirs.peekFirst();
            if( file != null )
            {
                backButton.setToolTipText( file.getAbsolutePath() );
            }
            else
            {
                backButton.setToolTipText( BACKWARD_TIP );
            }
        }
    }

    /***************************************************************************
     * Callback listener invoked when the next button is clicked.
     * @param e The ignored (can be null) ActionEvent that occurred.
     **************************************************************************/
    public void listener_nextButton_actionPerformed( ActionEvent e )
    {
        File file = ( File )nextDirs.pollFirst();
        if( file != null )
        {
            lastDirs.push( currentDirectory );

            backButton.setEnabled( true );
            backButton.setToolTipText( currentDirectory.getAbsolutePath() );
            nextButton.setEnabled( !nextDirs.isEmpty() );
            backButton.setToolTipText( currentDirectory.getAbsolutePath() );

            setDirectory( file );

            file = ( File )nextDirs.peekFirst();
            if( file != null )
            {
                nextButton.setToolTipText( file.getAbsolutePath() );
            }
            else
            {
                nextButton.setToolTipText( FORWARD_TIP );

            }
        }
    }

    /***************************************************************************
     * Callback listener invoked when the file tree has been selected or
     * deselected by either the user or programatically.
     * @param e The ignored (can be null) TreeSelectionEvent that occurred.
     **************************************************************************/
    public void listener_dirTree_valueChanged( TreeSelectionEvent e )
    {
        File[] dirsSelected = dirTree.getSelected();
        if( dirsSelected.length == 1 )
        {
            File f = dirsSelected[dirsSelected.length - 1];
            setDirectory( f, false );
        }
    }

    /***************************************************************************
     * Callback listener invoked when the up button is pressed.
     * @param e The ignored (can be null) ActionEvent that occurred.
     **************************************************************************/
    public void listener_upButton_actionPerformed( ActionEvent e )
    {
        File parent = getDirectory().getParentFile();
        if( parent != null )
        {
            setDirectory( parent );
            addLastFile();
        }
    }

    /***************************************************************************
     * Callback listener invoked when the refresh button is pressed.
     * @param e The ignored (can be null) ActionEvent that occurred.
     **************************************************************************/
    public void listener_refreshButton_actionPerformed( ActionEvent e )
    {
        ;
    }

    /***************************************************************************
     * Callback listener invoked when any button of the mouse is clicked while
     * the cursor is above the directory tree.
     * @param e The ignored (can be null) ActionEvent that occurred.
     **************************************************************************/
    public void listener_dirTree_mouseClicked( MouseEvent e )
    {
        File[] dirsSelected = dirTree.getSelected();

        if( dirsSelected != null && dirsSelected.length > 0 )
        {
            addLastFile();
        }
    }

    public void checkForMarkedImages() {
		if ( getMarkedImages().size() > 0 ) {
		    if ( JOptionPane.showConfirmDialog(this, 
		        "There is a list of marked images. Do you want to save the list?", "Save Marked Images List?", 
		        JOptionPane.YES_NO_OPTION,
		        JOptionPane.QUESTION_MESSAGE ) == JOptionPane.YES_OPTION) {
		    	saveMarkedList();
		    }
		}
    }
    
	public void saveMarkedList() {
    	JFileChooser fileChooser = new JFileChooser();
    	fileChooser.setDialogTitle("Specify a file to save");
    	fileChooser.setCurrentDirectory(getDirectory());
    	 
    	int userSelection = fileChooser.showSaveDialog(this);
    	 
    	if (userSelection == JFileChooser.APPROVE_OPTION) {
    	    File fileToSave = fileChooser.getSelectedFile();
			System.out.println("Saving marked images to file >> " + fileToSave.getAbsolutePath() + " <<");
    	    
			String pathPrefix = getDirectory().getAbsolutePath() + "/";
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileToSave, true));) {
			    for (String content : getMarkedImages()) {
			    	String outString = content.replace(pathPrefix, "");
			    	System.out.println("Writing marked image >> " + outString + " <<");
			    	bw.append(outString);
			    	bw.newLine();
			    }
			} catch (Exception e) {
				e.printStackTrace();
			}
			getMarkedImages().clear();
			saveMarkedImagesListMenuItem.setEnabled(false);
    	}
    }
}

class BackButtonActionHandler implements ActionListener
{
    private BeroViewExplorer adaptee;

    public BackButtonActionHandler( BeroViewExplorer adaptee )
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e )
    {
        adaptee.listener_backButton_actionPerformed( e );
    }
}

class NextButtonActionHandler implements ActionListener
{
    private BeroViewExplorer adaptee;

    public NextButtonActionHandler( BeroViewExplorer adaptee )
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e )
    {
        adaptee.listener_nextButton_actionPerformed( e );
    }
}

class OptionMenuItemActionHandler implements ActionListener
{
    private BeroViewExplorer adaptee;

    public OptionMenuItemActionHandler( BeroViewExplorer adaptee )
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e )
    {
        FileConfigurationDialog dialog = FileConfigurationDialog.showDialog( adaptee );
        dialog.getClass();
    }
}

class SaveMarkedImagesMenuListener implements ActionListener
{
    private BeroViewExplorer adaptee;

    public SaveMarkedImagesMenuListener( BeroViewExplorer adaptee )
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e )
    {
		if ( adaptee.getMarkedImages().size() > 0 ) {
	    	adaptee.saveMarkedList();
		}
    }
}

class AddressFieldActionHandler implements ActionListener
{
    private BeroViewExplorer adaptee;

    public AddressFieldActionHandler( BeroViewExplorer adaptee )
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e )
    {
        String addy = adaptee.getAddress();
        File file = new File( addy );

        if( file.isDirectory() )
        {
            adaptee.setDirectory( file );
        }
        else
        {
            JOptionPane.showMessageDialog( adaptee, file.getAbsolutePath() +
                " is not a directory!", "ERROR", JOptionPane.ERROR_MESSAGE );
        }
    }
}

class UpButtonActionHandler implements ActionListener
{
    private BeroViewExplorer adaptee;

    public UpButtonActionHandler( BeroViewExplorer adaptee )
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e )
    {
        adaptee.listener_upButton_actionPerformed( e );
    }
}

class RefreshButtonActionHandler implements ActionListener
{
    private BeroViewExplorer adaptee;

    public RefreshButtonActionHandler( BeroViewExplorer adaptee )
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed( ActionEvent e )
    {
        adaptee.listener_refreshButton_actionPerformed( e );
    }
}

class DirTreeSelectionHandler implements TreeSelectionListener
{
    private BeroViewExplorer adaptee;

    public DirTreeSelectionHandler( BeroViewExplorer adaptee )
    {
        this.adaptee = adaptee;
    }

    public void valueChanged( TreeSelectionEvent e )
    {
        adaptee.listener_dirTree_valueChanged( e );
    }
}

class DirTreeMouseHandler extends MouseAdapter
{
    private BeroViewExplorer adaptee;

    public DirTreeMouseHandler( BeroViewExplorer adaptee )
    {
        this.adaptee = adaptee;
    }

    public void mouseClicked( MouseEvent e )
    {
        adaptee.listener_dirTree_mouseClicked( e );
    }
}

class FileTableMouseHandler extends MouseAdapter
{
    private BeroViewExplorer adaptee;

    public FileTableMouseHandler( BeroViewExplorer adaptee )
    {
        this.adaptee = adaptee;
    }

    public void mouseClicked( MouseEvent e )
    {
        adaptee.listener_fileTable_mouseClicked( e );
    }

	public void mousePressed(MouseEvent e)
	{
		adaptee.listener_fileTable_mousePressed(e);
	}

	public void mouseReleased(MouseEvent e)
	{
		adaptee.listener_fileTable_mouseReleased(e);
	}
}

class FileTableSelectionHandler implements ListSelectionListener
{
	private BeroViewExplorer adaptee;
	
	public FileTableSelectionHandler( BeroViewExplorer adaptee )
	{
		this.adaptee = adaptee;
		
	}
	
	public void valueChanged(ListSelectionEvent e)
	{
		adaptee.listener_fileTable_valueChanged( e );
	}
}

class FileTableKeyHandler extends KeyAdapter
{
	private BeroViewExplorer adaptee;
	
	public FileTableKeyHandler( BeroViewExplorer adaptee )
	{
		this.adaptee = adaptee;
	}
	
    public void keyPressed(KeyEvent e)
    {
    	try {
			adaptee.listener_fileTable_keyPressed(e);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    }
}
