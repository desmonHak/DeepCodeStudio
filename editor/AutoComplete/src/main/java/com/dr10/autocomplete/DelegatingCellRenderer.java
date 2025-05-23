/*
 * 12/22/2008
 *
 * DelegatingCellRenderer.java - A renderer for Completions that will delegate
 * to the Completion's provider's renderer, if there is one.
 *
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package com.dr10.autocomplete;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * List cell renderer that delegates to a {@link CompletionProvider}'s
 * renderer, if it has one.  If it doesn't, it calls into a fallback renderer.
 * If a fallback renderer isn't specified, it simply renders
 * <code>(({@link Completion})value).toString()</code>.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class DelegatingCellRenderer extends DefaultListCellRenderer {

	/**
	 * The renderer to fall back on if one isn't specified by a provider.
	 * This is usually {@code this}.
	 */
	private ListCellRenderer<Object> fallback;


	/**
	 * Returns the fallback cell renderer.
	 *
	 * @return The fallback cell renderer.
	 * @see #setFallbackCellRenderer(ListCellRenderer)
	 */
	public ListCellRenderer<Object> getFallbackCellRenderer() {
		return fallback;
	}


	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
						int index, boolean selected, boolean hasFocus) {
		Completion c = (Completion)value;
		CompletionProvider p = c.getProvider();
		ListCellRenderer<Object> r = p.getListCellRenderer();
		if (r!=null) {
			return r.getListCellRendererComponent(list, value, index, selected,
													hasFocus);
		}
		if (fallback==null) {
			return super.getListCellRendererComponent(list, value, index,
												selected, hasFocus);
		}
		return fallback.getListCellRendererComponent(list, value, index,
													selected, hasFocus);
	}


	/**
	 * Sets the fallback cell renderer.
	 *
	 * @param fallback The fallback cell renderer.  If this is
	 *        <code>null</code>, {@code this} will be used.
	 * @see #getFallbackCellRenderer()
	 */
	public void setFallbackCellRenderer(ListCellRenderer<Object> fallback) {
		this.fallback = fallback;
	}


	@Override
	public void updateUI() {
		super.updateUI();
		if ((fallback instanceof JComponent) && fallback!=this) {
			((JComponent)fallback).updateUI();
		}
	}


}
