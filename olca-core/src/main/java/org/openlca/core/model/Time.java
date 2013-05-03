/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * <p style="margin-top: 0">
 * The time object contains information on the time the process is valid
 * </p>
 */
@Entity
@Table(name = "tbl_times")
public class Time extends AbstractEntity implements Copyable<Time> {

	/**
	 * <p style="margin-top: 0">
	 * An additional comment on the dates
	 * </p>
	 */
	@Lob
	@Column(name = "comment")
	private String comment;

	/**
	 * <p style="margin-top: 0">
	 * The time the validity of the process ends
	 * </p>
	 */
	@Temporal(value = TemporalType.DATE)
	@Column(name = "enddate")
	private Date endDate;

	/**
	 * <p style="margin-top: 0">
	 * The starting date the process is valid from
	 * </p>
	 */
	@Temporal(value = TemporalType.DATE)
	@Column(name = "startdate")
	private Date startDate;

	/**
	 * <p style="margin-top: 0">
	 * The property change support of the time object
	 * </p>
	 */
	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	/**
	 * <p style="margin-top: 0">
	 * Creates a new time object
	 * </p>
	 */
	public Time() {
	}

	/**
	 * <p style="margin-top: 0">
	 * Creates a new time object for the given process
	 * 
	 * @param process
	 *            The owner process of the time object
	 *            </p>
	 */
	public Time(final Process process) {
		setId(process.getId());
	}

	/**
	 * <p style="margin-top: 0">
	 * Adds a property change listener to the support
	 * 
	 * @param listener
	 *            The property change listener to be added
	 *            </p>
	 */
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the comment-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         An additional comment on the dates
	 *         </p>
	 */
	public String getComment() {
		return comment;
	}

	@Override
	public Time copy() {
		final Time time = new Time();
		time.setComment(getComment());
		time.setEndDate(getEndDate());
		time.setStartDate(getStartDate());
		return time;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the endDate-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The time the validity of the process ends
	 *         </p>
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * <p style="margin-top: 0">
	 * Getter of the startDate-field
	 * </p>
	 * 
	 * @return <p style="margin-top: 0">
	 *         The starting date the process is valid from
	 *         </p>
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * <p style="margin-top: 0">
	 * Removes a property change listener from the support
	 * 
	 * @param listener
	 *            The property change listener to be removed
	 *            </p>
	 */
	public void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the comment-field
	 * </p>
	 * 
	 * @param comment
	 *            <p style="margin-top: 0">
	 *            An additional comment on the dates
	 *            </p>
	 */
	public void setComment(final String comment) {
		support.firePropertyChange("comment", this.comment,
				this.comment = comment);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the endDate-field
	 * </p>
	 * 
	 * @param endDate
	 *            <p style="margin-top: 0">
	 *            The time the validity of the process ends
	 *            </p>
	 */
	public void setEndDate(final Date endDate) {
		support.firePropertyChange("endDate", this.endDate,
				this.endDate = endDate);
	}

	/**
	 * <p style="margin-top: 0">
	 * Setter of the startDate-field
	 * </p>
	 * 
	 * @param startDate
	 *            <p style="margin-top: 0">
	 *            The starting date the process is valid from
	 *            </p>
	 */
	public void setStartDate(final Date startDate) {
		support.firePropertyChange("startDate", this.startDate,
				this.startDate = startDate);
	}

}
