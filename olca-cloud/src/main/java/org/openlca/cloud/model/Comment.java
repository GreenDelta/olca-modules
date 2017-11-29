package org.openlca.cloud.model;

import java.util.Date;

import org.openlca.core.model.ModelType;

public class Comment {
	
	public long id;
	public String user;
	public String text;
	public String refId;
	public ModelType type;
	public String path;
	public Date date;
	public boolean released;
	public boolean approved;
	public long replyTo;
	
}
