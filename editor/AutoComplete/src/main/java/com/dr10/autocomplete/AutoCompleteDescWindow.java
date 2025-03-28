/*
 * 12/21/2008
 *
 * AutoCompleteDescWindow.java - A window containing a description of the
 * currently selected completion.
 *
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package com.dr10.autocomplete;

import org.fife.ui.rsyntaxtextarea.PopupWindowDecorator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;


/**
 * The optional "description" window that describes the currently selected
 * item in the auto-completion window.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class AutoCompleteDescWindow extends JWindow implements HyperlinkListener,
        DescWindowCallback {

	/**
	 * The parent AutoCompletion instance.
	 */
	private AutoCompletion ac;

	/**
	 * Renders the HTML description.
	 */
	private JEditorPane descArea;

	/**
	 * The scroll pane that {@link #descArea} is in.
	 */
	private JScrollPane scrollPane;

	/**
	 * The toolbar with "back" and "forward" buttons.
	 */
	private JToolBar descWindowNavBar;

	/**
	 * Action that goes to the previous description displayed.
	 */
	private Action backAction;

	/**
	 * Action that goes to the next description displayed.
	 */
	private Action forwardAction;

	/**
	 * History of descriptions displayed.
	 */
	private List<HistoryEntry> history;

	/**
	 * The current position in {@link #history}.
	 */
	private int historyPos;

	/**
	 * Provides a slight delay between asking to set a description and actually
	 * displaying it, so that if the user is scrolling quickly through
	 * completions, those with slow-to-calculate summaries won't bog down the
	 * scrolling.
	 */
	private Timer timer;

	/**
	 * The action that listens for the timer to fire.
	 */
	private TimerAction timerAction;

	/**
	 * The resource bundle for this window.
	 */
	private ResourceBundle bundle;

	/**
	 * The amount of time to wait after the user changes the selected
	 * completion to refresh the description.  This delay is in place to help
	 * performance for {@link Completion}s that may be slow to compute their
	 * summary text.
	 */
	private static final int INITIAL_TIMER_DELAY			= 120;

	/**
	 * The resource bundle name.
	 */
	private static final String MSG =
					"org.fife.ui.autocomplete.AutoCompleteDescWindow";

	private static final String FLAT_LAF_BORDER_PREFIX = "com.formdev.flatlaf.ui.Flat";


	/**
	 * Constructor.
	 *
	 * @param owner The parent window.
	 * @param ac The parent auto-completion.
	 */
	AutoCompleteDescWindow(Window owner, AutoCompletion ac) {

		super(owner);
		this.ac = ac;

		ComponentOrientation o = ac.getTextComponentOrientation();

		JPanel cp = new JPanel(new BorderLayout());
		cp.setBorder(getToolTipBorder());

		descArea = new JEditorPane("text/html", null);
		TipUtil.tweakTipEditorPane(descArea);
		descArea.addHyperlinkListener(this);
		scrollPane = new JScrollPane(descArea);
		Border b = BorderFactory.createEmptyBorder();
		descArea.setBackground(ac.getDescWindowColor());
		scrollPane.setBorder(b);
		scrollPane.setViewportBorder(b);
		scrollPane.setBackground(descArea.getBackground());
		scrollPane.getViewport().setBackground(descArea.getBackground());
		cp.add(scrollPane);

		descWindowNavBar = new JToolBar();
		backAction = new ToolBarBackAction(o.isLeftToRight());
		forwardAction = new ToolBarForwardAction(o.isLeftToRight());
		descWindowNavBar.setFloatable(false);
		descWindowNavBar.setBackground(ac.getDescWindowColor());
		descWindowNavBar.add(new JButton(backAction));
		descWindowNavBar.add(new JButton(forwardAction));

		JPanel bottomPanel = new JPanel(new BorderLayout());
		b = new AbstractBorder() {
			@Override
			public Insets getBorderInsets(Component c) {
				return new Insets(1, 0, 0, 0);
			}
			@Override
			public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
				g.setColor(UIManager.getColor("controlDkShadow"));
				g.drawLine(x,y, x+w-1,y);
			}
		};
		bottomPanel.setBorder(b);
		bottomPanel.setBackground(ac.getDescWindowColor());
		SizeGrip rp = new SizeGrip();
		bottomPanel.add(descWindowNavBar, BorderLayout.LINE_START);
		bottomPanel.add(rp, BorderLayout.LINE_END);
		rp.setBackground(ac.getDescWindowColor());
		cp.add(bottomPanel, BorderLayout.SOUTH);
		setContentPane(cp);

		applyComponentOrientation(o);
		setFocusableWindowState(false);

		// Give apps a chance to decorate us with drop shadows, etc.
		if (Util.getShouldAllowDecoratingMainAutoCompleteWindows()) {
			PopupWindowDecorator decorator = PopupWindowDecorator.get();
			if (decorator!=null) {
				decorator.decorate(this);
			}
		}

		history = new ArrayList<>(1); // Usually small
		historyPos = -1;

		timerAction = new TimerAction();
		timer = new Timer(INITIAL_TIMER_DELAY, timerAction);
		timer.setRepeats(false);

	}


	/**
	 * Sets the currently displayed description and updates the history.
	 *
	 * @param historyItem The item to add to the history.
	 */
	private void addToHistory(HistoryEntry historyItem) {
		history.add(++historyPos, historyItem);
		clearHistoryAfterCurrentPos();
		setActionStates();
	}


	/**
	 * Clears the history of viewed descriptions.
	 */
	private void clearHistory() {
		history.clear(); // Try to free some memory.
		historyPos = -1;
		if (descWindowNavBar!=null) {
			setActionStates();
		}
	}


	/**
	 * Makes the current history page the last one in the history.
	 */
	private void clearHistoryAfterCurrentPos() {
		for (int i=history.size()-1; i>historyPos; i--) {
			history.remove(i);
		}
		setActionStates();
	}


	/**
	 * Copies from the description text area, if it is visible and there is
	 * a selection.
	 *
	 * @return Whether a copy occurred.
	 */
	public boolean copy() {
		if (isVisible() &&
				descArea.getSelectionStart()!=descArea.getSelectionEnd()) {
			descArea.copy();
			return true;
		}
		return false;
	}


	/**
	 * FlatLaf adds insets to tool tips, and for some themes (usually light ones)
	 * also uses a line border, whereas for other themes (usually dark ones)
	 * there is no line border.  We need to ensure our border has no insets
	 * so our draggable bottom component looks good, but we'd like to preserve
	 * the color of the line border, if any.  This method allows us to do so
	 * without a compile-time dependency on flatlaf.
	 *
	 * @param border The default tool tip border for the current Look and Feel.
	 * @return The border to use for this window.
	 */
	private static Border getReplacementForFlatLafBorder(Border border) {

		Class<?> clazz = border.getClass();

		// If it's a FlatLineBorder, get its color.
		// If it's a FlatEmptyBorder, just return a 0-sized regular EmptyBorder.
		Color color = null;
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			if ("getLineColor".equals(method.getName())) {
				try {
					color = (Color)method.invoke(border);
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace(); // Never happens
				}
			}
		}

		if (color != null) {
			return BorderFactory.createLineBorder(color);
		}
		return BorderFactory.createEmptyBorder();
	}


	/**
	 * Returns the localized message for the specified key.
	 *
	 * @param key The key.
	 * @return The localized message.
	 */
	private String getString(String key) {
		if (bundle==null) {
			bundle = ResourceBundle.getBundle(MSG);
		}
		return bundle.getString(key);
	}


	private static Border getToolTipBorder() {

		Border border = org.fife.ui.rsyntaxtextarea.focusabletip.TipUtil.getToolTipBorder();


		// Special case for FlatDarkLaf and FlatLightLaf, since they add an
		// empty border to tool tips that messes up our floating-window appearance
		if (isFlatLafBorder(border)) {
			border = getReplacementForFlatLafBorder(border);
		}

		return border;
	}


	/**
	 * Called when a hyperlink is clicked.
	 *
	 * @param e The event.
	 */
	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {

		HyperlinkEvent.EventType type = e.getEventType();
		if (!type.equals(HyperlinkEvent.EventType.ACTIVATED)) {
			return;
		}

		// Users can redirect URLs, perhaps to a local copy of documentation.
		URL url = e.getURL();
		if (url!=null) {
			LinkRedirector redirector = AutoCompletion.getLinkRedirector();
			if (redirector!=null) {
				URL newUrl = redirector.possiblyRedirect(url);
				if (newUrl!=null && newUrl!=url) {
					url = newUrl;
					e = new HyperlinkEvent(e.getSource(), e.getEventType(),
							newUrl, e.getDescription(), e.getSourceElement());
				}
			}
		}

		// Custom hyperlink handler for this completion type
		ExternalURLHandler handler = ac.getExternalURLHandler();
		if (handler!=null) {
			HistoryEntry current = history.get(historyPos);
			handler.urlClicked(e, current.completion, this);
			return;
		}

		// No custom handler...
		if (url!=null) {
			// Try loading in external browser (Java 6+ only).
			try {
				Util.browse(new URI(url.toString()));
			} catch (/*IO*/URISyntaxException ioe) {
				UIManager.getLookAndFeel().provideErrorFeedback(descArea);
				ioe.printStackTrace();
			}
		}
		else { // Assume simple function name text, like in c.xml
			// FIXME: This is really a hack, and we assume we can find the
			// linked-to item in the same CompletionProvider.
			AutoCompletePopupWindow parent =
							(AutoCompletePopupWindow)getParent();
			CompletionProvider p = parent.getSelection().getProvider();
			if (p instanceof AbstractCompletionProvider) {
				String name = e.getDescription();
				List<Completion> l = ((AbstractCompletionProvider)p).
									getCompletionByInputText(name);
				if (l!=null && !l.isEmpty()) {
					// Just use the 1st one if there's more than 1
					Completion c = l.get(0);
					setDescriptionFor(c, true);
				}
				else {
					UIManager.getLookAndFeel().provideErrorFeedback(descArea);
				}
			}
		}

	}


	private static boolean isFlatLafBorder(Border border) {
		return border != null && border.getClass().getName().startsWith(FLAT_LAF_BORDER_PREFIX);
	}


	/**
	 * Enables or disables the back and forward actions as appropriate.
	 */
	private void setActionStates() {
		// TODO: Localize this text!
		String desc = null;
		if (historyPos>0) {
			backAction.setEnabled(true);
			desc = "Back to " + history.get(historyPos-1);
		}
		else {
			backAction.setEnabled(false);
		}
		backAction.putValue(Action.SHORT_DESCRIPTION, desc);
		if (historyPos>-1 && historyPos<history.size()-1) {
			forwardAction.setEnabled(true);
			desc = "Forward to " + history.get(historyPos+1);
		}
		else {
			forwardAction.setEnabled(false);
			desc = null;
		}
		forwardAction.putValue(Action.SHORT_DESCRIPTION, desc);
	}


	/**
	 * Sets the description displayed in this window.  This clears the
	 * history.
	 *
	 * @param item The item whose description you want to display.
	 */
	public void setDescriptionFor(Completion item) {
		setDescriptionFor(item, false);
	}


	/**
	 * Sets the description displayed in this window.
	 *
	 * @param item The item whose description you want to display.
	 * @param addToHistory Whether to add this page to the page history
	 *        (as opposed to clearing it and starting anew).
	 */
	protected void setDescriptionFor(Completion item, boolean addToHistory) {
		setDescriptionFor(item, null, addToHistory);
	}


	/**
	 * Sets the description displayed in this window.
	 *
	 * @param item The item whose description you want to display.
	 * @param anchor The anchor to jump to, or <code>null</code> if none.
	 * @param addToHistory Whether to add this page to the page history
	 *        (as opposed to clearing it and starting anew).
	 */
	protected void setDescriptionFor(Completion item, String anchor,
									boolean addToHistory) {
		timer.stop();
		timerAction.setCompletion(item, anchor, addToHistory);
		timer.start();
	}


	private void setDisplayedDesc(Completion completion, final String anchor,
									boolean addToHistory) {

		String desc = completion==null ? null : completion.getSummary();
		if (desc==null) {
			desc = "<html><em>" + getString("NoDescAvailable") + "</em>";
		}
		descArea.setText(desc);
		if (anchor!=null) {
			SwingUtilities.invokeLater(() -> descArea.scrollToReference(anchor));
		}
		else {
			descArea.setCaretPosition(0); // In case of scrolling
		}

		if (!addToHistory) {
			// Remove everything first if this is going to be the only
			// thing in history.
			clearHistory();
		}
		addToHistory(new HistoryEntry(completion, desc, null));

	}


	@Override
	public void setVisible(boolean visible) {
		if (!visible) {
			clearHistory();
		}
		super.setVisible(visible);
	}


	/**
	 * Callback for custom <code>ExternalURLHandler</code>s.
	 *
	 * @param completion The completion to display.
	 * @param anchor The anchor in the HTML to jump to, or <code>null</code>
	 *        if none.
	 */
	@Override
	public void showSummaryFor(Completion completion, String anchor) {
		setDescriptionFor(completion, anchor, true);
	}


	/**
	 * Called by the parent completion popup window the LookAndFeel is updated.
	 */
	public void updateUI() {
		SwingUtilities.updateComponentTreeUI(this);
		// Update editor pane for new font, bg, selection colors, etc.
		TipUtil.tweakTipEditorPane(descArea);
		scrollPane.setBackground(descArea.getBackground());
		scrollPane.getViewport().setBackground(descArea.getBackground());
		((JPanel)getContentPane()).setBorder(TipUtil.getToolTipBorder());
	}


	/**
	 * A completion and its cached summary text.
	 */
	private static class HistoryEntry {

		private Completion completion;
		private String summary;
		private String anchor;

		HistoryEntry(Completion completion, String summary,
									String anchor) {
			this.completion = completion;
			this.summary = summary;
			this.anchor = anchor;
		}

		/**
		 * Overridden to display a short name for the completion, since it's
		 * used in the tool tips for the "back" and "forward" buttons.
		 *
		 * @return A string representation of this history entry.
		 */
		@Override
		public String toString() {
			return completion.getInputText();
		}

	}


	/**
	 * Action that actually updates the summary text displayed.
	 */
	private class TimerAction extends AbstractAction {

		private Completion completion;
		private String anchor;
		private boolean addToHistory;

		/**
		 * Called when the timer is fired.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			setDisplayedDesc(completion, anchor, addToHistory);
		}

		void setCompletion(Completion c, String anchor,
									boolean addToHistory) {
			this.completion = c;
			this.anchor = anchor;
			this.addToHistory = addToHistory;
		}

	}


	/**
	 * Action that moves to the previous description displayed.
	 */
	class ToolBarBackAction extends AbstractAction {

		ToolBarBackAction(boolean ltr) {
			String img = "org/fife/ui/autocomplete/arrow_" +
						(ltr ? "left.png" : "right.png");
			ClassLoader cl = getClass().getClassLoader();
			Icon icon = new ImageIcon(cl.getResource(img));
			putValue(Action.SMALL_ICON, icon);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (historyPos>0) {
				HistoryEntry pair = history.get(--historyPos);
				descArea.setText(pair.summary);
				if (pair.anchor!=null) {
					//System.out.println("Scrolling to: " + pair.anchor);
					descArea.scrollToReference(pair.anchor);
				}
				else {
					descArea.setCaretPosition(0);
				}
				setActionStates();
			}
		}

	}


	/**
	 * Action that moves to the previous description displayed.
	 */
	class ToolBarForwardAction extends AbstractAction {

		ToolBarForwardAction(boolean ltr) {
			String img = "org/fife/ui/autocomplete/arrow_" +
							(ltr ? "right.png" : "left.png");
			ClassLoader cl = getClass().getClassLoader();
			Icon icon = new ImageIcon(cl.getResource(img));
			putValue(Action.SMALL_ICON, icon);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (history!=null && historyPos<history.size()-1) {
				HistoryEntry pair = history.get(++historyPos);
				descArea.setText(pair.summary);
				if (pair.anchor!=null) {
					//System.out.println("Scrolling to: " + pair.anchor);
					descArea.scrollToReference(pair.anchor);
				}
				else {
					descArea.setCaretPosition(0);
				}
				setActionStates();
			}
		}

	}


}
