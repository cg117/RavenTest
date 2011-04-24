package raven.edit.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.tools.Tool;
import raven.edit.editor.actions.*;
import raven.edit.tools.EditorTool;
import raven.game.RavenBot;
import raven.game.RavenMap;
import raven.game.triggers.Trigger;
import raven.math.Vector2D;
import raven.math.Wall2D;

public class EditorView extends JFrame implements ViewportDelegate {
	private RavenMap level;
	private EditorViewDelegate delegate;
	private Viewport viewport;
	private JLabel statusBar;
	
	// Actions
	private NewLevelAction newLevelAction;
	private OpenLevelAction openLevelAction;
	private SaveLevelAction saveLevelAction;
	private SelectToolAction selectToolAction;
	private WallToolAction wallToolAction;
	private DoorToolAction doorToolAction;
	private SpawnToolAction spawnToolAction;
	private HealthToolAction healthToolAction;
	private GraphToolAction graphToolAction;
	private RocketToolAction rocketToolAction;
	private ShotgunToolAction shotgunToolAction;
	private RailgunToolAction railgunToolAction;
	
	public EditorView(RavenMap theLevel) {
		level = theLevel;
	}
	
	public void create() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setPreferredSize(new Dimension(800, 600));
		this.setLayout(new BorderLayout());
		
		// First lets make the actions we'll want to use
		createActions();
		// Menus
		createMenu();
		// Lets add a toolbar
		createToolbar();
		// Add another toolbar for the inspector
		createInspector();
		// Add a statusbar
		statusBar = new JLabel("Initialized.");
		statusBar.setPreferredSize(new Dimension(100, 16));
		this.add(statusBar, BorderLayout.SOUTH);
		
		// Add a viewport
		viewport = new Viewport(level);
		viewport.setDelegate(this);
		this.add(viewport);
	
		
		this.pack();
        this.setTitle("Editing"); // TODO add names to maps!
        this.setVisible(true);
	}
	
	private void createActions() {
		newLevelAction = new NewLevelAction("New",
											new ImageIcon("images/new.png"),
											"Create an empty level.",
											new Integer(KeyEvent.VK_N),
											delegate);
		openLevelAction = new OpenLevelAction("Open",
											new ImageIcon("images/open.png"),
											"Open an existing level.",
											new Integer(KeyEvent.VK_O),
											delegate);
		saveLevelAction = new SaveLevelAction("Save",
											new ImageIcon("images/save.png"),
											"Save the current level.",
											new Integer(KeyEvent.VK_S),
											delegate);
		
		selectToolAction = new SelectToolAction("Select",
											new ImageIcon("images/select.png"),
											"Select objects within the level.",
											new Integer(KeyEvent.VK_1),
											delegate);
		wallToolAction = new WallToolAction("Add walls",
											new ImageIcon("images/wall.png"),
											"Draw new walls into the level.",
											new Integer(KeyEvent.VK_2),
											delegate);
		
		doorToolAction = new DoorToolAction("Add doors",
											new ImageIcon("images/door.png"),
											"Draw new doors into the level.",
											new Integer(KeyEvent.VK_3),
											delegate);
		spawnToolAction = new SpawnToolAction("Add spawn points",
											new ImageIcon("images/spawn.png"),
											"Create new locations where bots will enter the world.",
											new Integer(KeyEvent.VK_4),
											delegate);
		graphToolAction = new GraphToolAction("Add graph node",
											new ImageIcon("images/graph.png"),
											"Insert a new graph node into the navigation network.",
											new Integer(KeyEvent.VK_5),
											delegate);
		healthToolAction = new HealthToolAction("Add a health spanwer.",
											new ImageIcon("images/health.png"),
											"Add locations where health will respawn.",
											new Integer(KeyEvent.VK_6),
											delegate);
		rocketToolAction = new RocketToolAction("Add a rocket launcher spanwer",
											new ImageIcon("images/rocket.png"),
											"Add locations where rocket launchers will respawn.",
											new Integer(KeyEvent.VK_7),
											delegate);
		shotgunToolAction = new ShotgunToolAction("Add a shotgun spawner",
											new ImageIcon("images/shotgun.png"),
											"Add locations where shotguns will respawn.",
											new Integer(KeyEvent.VK_8),
											delegate);
		railgunToolAction = new RailgunToolAction("Add a railgun spawner",
											new ImageIcon("images/railgun.png"),
											"Add locations where railguns will respawn.",
											new Integer(KeyEvent.VK_9),
											delegate);
	}
	
	private void createMenu() {
		JMenu menu, subMenu;
		JMenuItem menuItem;
		JCheckBoxMenuItem checkedMenuItem;
		
		// Create the menu bar
		JMenuBar menuBar = new JMenuBar();
		
		// File
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);
		// File->New
		menuItem = new JMenuItem("New...", KeyEvent.VK_N);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.META_MASK));
		menu.add(menuItem);
		// File->Open
		menuItem = new JMenuItem("Open...", KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.META_MASK));
		menu.add(menuItem);
		// File->Save
		menuItem = new JMenuItem("Save", KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.META_MASK));
		menu.add(menuItem);
		// File->Save As...
		menuItem = new JMenuItem("Save As...", KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.META_MASK | ActionEvent.SHIFT_MASK));
		menu.add(menuItem);
		
		// Edit
		menu = new JMenu("Edit");
		menu.setMnemonic(KeyEvent.VK_E);
		menuBar.add(menu);
		// Edit->Undo
		menuItem = new JMenuItem("Undo", KeyEvent.VK_Z);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.META_MASK));
		menuItem.setEnabled(false);
		menu.add(menuItem);
		// Edit->Redo
		menuItem = new JMenuItem("Redo", KeyEvent.VK_Z);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.SHIFT_MASK | ActionEvent.META_MASK));
		menuItem.setEnabled(false);
		menu.add(menuItem);
		// Edit->Delete selected
		menuItem = new JMenuItem("Delete selected", KeyEvent.VK_DELETE);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		menu.add(menuItem);
		// Tools
		menu = new JMenu("Tools");
		menu.setMnemonic(KeyEvent.VK_T);
		menuBar.add(menu);
		// Tools->Grid On
		checkedMenuItem = new JCheckBoxMenuItem("Grid on");
		menu.add(checkedMenuItem);
		// Tools->Snap to Grid
		checkedMenuItem = new JCheckBoxMenuItem("Snap to grid");
		menu.add(checkedMenuItem);
		// Graph
		menu = new JMenu("Graph");
		menu.setMnemonic(KeyEvent.VK_G);
		menuBar.add(menu);
		// Graph->Clear graph
		menuItem = new JMenuItem("Clear graph");
		menu.add(menuItem);
		// Graph->Rebuild (connections based on current Max Edge Length)
		menuItem = new JMenuItem("Rebuild (connections based on current Max Edge Length)");
		menu.add(menuItem);
		// Graph->Grow from seed
		menuItem = new JMenuItem("Grow from seed");
		menu.add(menuItem);
		// Graph->Show Edges
		checkedMenuItem = new JCheckBoxMenuItem("Show Edges");
		menu.add(checkedMenuItem);
		// Graph->Show Indices
		checkedMenuItem = new JCheckBoxMenuItem("Show Indices");
		menu.add(checkedMenuItem);
		// Graph->Lock
		checkedMenuItem = new JCheckBoxMenuItem("Lock");
		menu.add(checkedMenuItem);
		// Graph->Max Edge Length
		subMenu = new JMenu("Max Edge Length");
		menu.add(subMenu);
		// Graph->Max Edge Length->[20, 40, 60, 80, 100, 150, 200, default]
		menuItem = new JMenuItem("20");
		subMenu.add(menuItem);
		menuItem = new JMenuItem("40");
		subMenu.add(menuItem);
		menuItem = new JMenuItem("60");
		subMenu.add(menuItem);
		menuItem = new JMenuItem("80");
		subMenu.add(menuItem);
		menuItem = new JMenuItem("100");
		subMenu.add(menuItem);
		menuItem = new JMenuItem("150");
		subMenu.add(menuItem);
		menuItem = new JMenuItem("200");
		subMenu.add(menuItem);
		menuItem = new JMenuItem("Default");
		subMenu.add(menuItem);

		this.setJMenuBar(menuBar);
	}
	
	private void createToolbar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		// And add some buttons
		ArrayList<AbstractButton> buttons = new ArrayList<AbstractButton>();
		buttons.add(new JButton(newLevelAction));
		buttons.add(new JButton(openLevelAction));
		buttons.add(new JButton(saveLevelAction));
		buttons.add(null);
		buttons.add(new JToggleButton(selectToolAction));
		buttons.add(new JToggleButton(graphToolAction));
		buttons.add(null);
		buttons.add(new JToggleButton(wallToolAction));
		buttons.add(new JToggleButton(doorToolAction));
		buttons.add(null);
		buttons.add(new JToggleButton(spawnToolAction));
		buttons.add(null);
		buttons.add(new JToggleButton(healthToolAction));
		buttons.add(new JToggleButton(rocketToolAction));
		buttons.add(new JToggleButton(shotgunToolAction));
		buttons.add(new JToggleButton(railgunToolAction));
		
		// Icons only if there is one
		ButtonGroup group = new ButtonGroup();
		for (int i = 0; i < buttons.size(); i++) {
			AbstractButton button = buttons.get(i);
			if (button == null) {
				toolbar.addSeparator();
			} else {
				if (button instanceof JToggleButton) {
					group.add(button);
				}
				if (button.getIcon() != null) {
					button.setText("");
				}
				toolbar.add(button);
			}
		}

		this.add(toolbar, BorderLayout.NORTH);
	}

	private void createInspector() {
/*		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(true);
		// And add some buttons
		JTextField textField = new JTextField(8);
		toolbar.add(textField);
		this.add(toolbar, BorderLayout.EAST);
*/	}

	public void setDelegate(EditorViewDelegate delegate) {
		this.delegate = delegate;
	}

	////////////////
	// Accessors
	
	public synchronized RavenMap getLevel() {
		return level;
	}

	public synchronized void setLevel(RavenMap level) {
		// TODO update the viewport's level!
		this.level = level;
		viewport.setLevel(level);
	}

	//////////////////////
	// Viewport delegate
	
	@Override
	public void updateStatus(String status) {
		statusBar.setText(status);		
	}

	@Override
	public void addWalls(Vector2D[] segments) {
		for (int i = 0; i < segments.length - 1; i++) {
			level.addWall(segments[i], segments[i+1]);
		}
		
		delegate.makeDirty();
	}

	public void setTool(EditorTool newTool) {
		viewport.setTool(newTool);
	}

	@Override
	public Viewport getViewport() {
		return viewport;
	}

	@Override
	public void setViewport(Viewport viewport) {
		this.viewport = viewport;
	}

	@Override
	public void addTrigger(Trigger<RavenBot> trigger) {
		level.getTriggers().add(trigger);
		
	}

}